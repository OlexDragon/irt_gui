package irt.gui.web.controllers;

import java.io.File;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import irt.gui.web.beans.Packet;
import irt.gui.web.beans.PacketGroupId;
import irt.gui.web.beans.PacketType;
import irt.gui.web.beans.RequestPacket;
import irt.gui.web.beans.upload.IrtProfile;
import irt.gui.web.beans.upload.TarToBytes;
import irt.gui.web.services.SerialPortDistributor;

@RestController
@RequestMapping("upgrade/rest")
public class UpgradeRestController {
	private final static Logger logger = LogManager.getLogger();
	private final static int BUFFER_SIZE = 2500;	//max 4095 - 4 bytes for counter; have to be less then 4095 because of byte stuffing
	private final static int DELAY = 300;	// ms between packets send
	private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(5);
	private static final long WAIT_TIME = TimeUnit.MINUTES.toMillis(5);

	public final static int PACKET_ID = (short) 555;
	public final static byte PARAMETER_ID_UPGRADE_START = 20;
	public final static byte PARAMETER_ID_UPGRADE_DATA = 21;

	@Autowired SerialPortDistributor distributor;
	private final RestTemplate restTemplate = new RestTemplate();

	@GetMapping("list")
	List<String> getIrtPackages(@RequestParam String upgradeName){
        String url = "https://irt-technologies-inc.onrender.com/pkg/list?f=" + upgradeName;
		return restTemplate.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {}).getBody();
	}

	private final static int REPEAT_COUNT = 2;
	@PostMapping
	String upgrade(@RequestParam String sp, Integer address, String name, String file) {
		logger.error("sp={}, name={}, address={}, file={}", sp, name, address, file);
		distributor.lockPort(sp);
        try {

        	// Load File from https://irt-technologies-inc.onrender.com/load POST method
			byte[] fileBytes = loadPkgBytes(name, file);

			return send(sp, address, fileBytes);

        }catch (Exception e) {
        	logger.catching(e);
        	return e.getClass().getSimpleName() +" : " + e.getLocalizedMessage();
         }finally {
        	 distributor.unlockPort(sp);
         }
	}

	@PostMapping("profile/{sp}/{address}")
	String upgradeProfile(@PathVariable String sp, @PathVariable Integer address , @RequestParam String p) {
		logger.traceEntry("sp={}, address={}, p={}", sp, address, p);

		distributor.lockPort(sp);
		final File file = Paths.get(URI.create(p.replaceAll(" ", "%20"))).toFile();
		if(!file.exists()) {
			logger.warn("File does not exist: {}", p);
			return "File does not exist: " + p;
		}

		try {

			final TarToBytes tarToBytes = new IrtProfile(file.toPath());
			return send(sp, address, tarToBytes.toBytes());

		} catch (Exception e) {
	       	logger.catching(e);
        	return e.getClass().getSimpleName() +" : " + e.getLocalizedMessage();
		}finally {
			distributor.unlockPort(sp);
        }
	}

	@RequestMapping("load")
	ResponseEntity<ByteArrayResource> load(@RequestParam String folder, @RequestParam String pkg) {

		final byte[] fileBytes = loadPkgBytes(folder, pkg);
        ByteArrayResource resource = new ByteArrayResource(fileBytes);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + pkg + "\"")
                .contentLength(fileBytes.length)
                .body(resource);
	}

	private String send(String sp, Integer address, byte[] fileBytes)
			throws InterruptedException, ExecutionException, TimeoutException {
		if(fileBytes==null || fileBytes.length==0)
			return "File is empty";

		RequestPacket responsePacket = sendUpgrateData(sp, address, PARAMETER_ID_UPGRADE_START, null);

		final int fileLength = fileBytes.length;

		int index = 0;
		int counter = 0;	// packet counter
		int repeat = REPEAT_COUNT;
		boolean end = false;
		byte[] data = null;
		while(true){

			final String error = responsePacket.getError();
			if (error!=null) {
				logger.error("Error received: {}", error);
				return error;
			}

			final Packet response = new Packet(responsePacket.getAnswer(), address==0);
			logger.debug("Sending packet #{}; tindex={}/{}", counter, index, fileLength);

			TimeUnit.MILLISECONDS.sleep(DELAY);	// wait before sending next packet

			final int errorCode = response.getError().getCode();
			if (errorCode != 0) {
				if (errorCode!=2 && repeat > 0) {	// if not Write Error and repeat count not exceeded
					repeat--;
					logger.warn("\nSend repeat #{}\n\tUpgrade failed: {},\n\t{}", response, REPEAT_COUNT - repeat, data);
					responsePacket = sendUpgrateData(sp, address, PARAMETER_ID_UPGRADE_DATA, data);
					continue;	// repeat sending;
				}
				return response.toString();
			}
			repeat = REPEAT_COUNT;

			if (end) break;	// exit condition
			end = index >= fileLength;	// reach the end of file

			final int size = Math.min(BUFFER_SIZE, fileLength - index);
			final int position = index + size;	// current position
			if (end) 
				data = new byte[4];	// last packet with no data
			else {
				final byte[] toSend = Arrays.copyOfRange(fileBytes, index, position);
				data = new byte[4 + toSend.length];
				System.arraycopy(toSend, 0, data, 4, toSend.length);
			}
			final byte[] bsCounter = ByteBuffer.allocate(4).putInt(counter++).array();
			data[0] = bsCounter[0];
			data[1] = bsCounter[1];
			data[2] = bsCounter[2];
			data[3] = bsCounter[3];
			
			responsePacket = sendUpgrateData(sp, address, PARAMETER_ID_UPGRADE_DATA, data);
		   	index = position;
		};

		return "Done";
	}

	private RequestPacket sendUpgrateData(String sp, Integer address, byte parameterId, byte[] data) throws InterruptedException, ExecutionException, TimeoutException {
		logger.debug("sendUpgrateData: sp={}, address={}, parameterId={}, data={}", sp, address, parameterId, data);
		final Packet p = new Packet(address.byteValue(),  PacketType.COMMAND, (short)PACKET_ID, PacketGroupId.CONTROL, parameterId, data);
		final RequestPacket requestPacket = new RequestPacket(true, PACKET_ID, address, sp, p.toSend(), "Upgrade");
		final long timeout = (parameterId==PARAMETER_ID_UPGRADE_DATA && data.length==4) ? WAIT_TIME : TIMEOUT;
//		logger.error("\nSending: \n\t{}\n\ttimeout: {}\n\t{}", p, timeout, data);
		requestPacket.setTimeout((int) timeout);
		final FutureTask<RequestPacket> dataSend = distributor.send(requestPacket);
		return dataSend.get(timeout, TimeUnit.MILLISECONDS);
	}

	@Value("${irt.site.url}") private String irtSiteUrl;

    private byte[] loadPkgBytes(String name, String file) {
        String url = irtSiteUrl + "/pkg/load?f=" + name + "&p=" + file;
        return restTemplate.postForObject(url, null, byte[].class);
    }
}
