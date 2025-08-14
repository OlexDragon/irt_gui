package irt.gui.web.beans;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import irt.gui.web.services.BytesToStringSerializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@RequiredArgsConstructor @Getter @Setter @ToString @EqualsAndHashCode(exclude = {"serialPort", "bytes", "function", "baudrate", "timeout", "answer", "error"}) @JsonIgnoreProperties(ignoreUnknown = true)
public class RequestPacket {

	@NonNull private Boolean command;
	@NonNull private Integer id;
	@NonNull private Integer unitAddr;
	@NonNull private String serialPort;
	@JsonSerialize(using = BytesToStringSerializer.class)
	@NonNull private byte[] bytes;
	@NonNull private String function;

	private String name;
	private Integer baudrate;
	private Integer timeout;
	@JsonSerialize(using = BytesToStringSerializer.class)
	private byte[] answer;
	private String error;
}
