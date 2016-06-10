package irt.gui.controllers.calibration.process;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.data.packet.Packet;
import irt.gui.data.packet.enums.PacketErrors;
import irt.gui.data.packet.interfaces.LinkedPacket;
import irt.gui.data.packet.interfaces.PacketToSend;

public class AverageInteger extends Average<Integer> {

	public AverageInteger(Tool tool, Commands command) {
		super(tool, command);
	}

	@Override public Integer call() throws Exception {

		List<Integer> list = new ArrayList<>();
		observer.setList(list);

		Integer a1 = Integer.MAX_VALUE;
		Integer a2 = Integer.MIN_VALUE;

		while(list.size()<5 || a1.compareTo(a2)!=0){
			a1 = a2;

			synchronized (this) {
				try {

					tool.get(command, observer);

					wait(111);

				} catch (Exception e) {
					logger.catching(e);
					break;
				}
			}

			a2 = getAverage(list);
		}

		return getAverage(list);
	}

	private Integer getAverage(List<Integer> list) {
		final List<Integer> l = new ArrayList<>(list);
		return (int) Math
				.round(
						l
						.parallelStream()
						.mapToInt(d->d.intValue())
						.average()
						.orElse(0.0));
	}

	private final ValueObserver observer = new ValueObserver();

	///****************************** class ValueObserver   ****************************
	private class ValueObserver implements Observer{

		private List<Integer> list;

		public void setList(List<Integer> list) {
			this.list= list;
		}

		@Override
		public void update(Observable o, Object arg) {
			EXECUTOR.execute(()->{
				o.deleteObserver(this);
				try{
					final PacketToSend p = (PacketToSend)o;
					final byte[] answer = p.getAnswer();
					if(answer!=null){
						final LinkedPacket newPacket = (LinkedPacket) Packet.createNewPacket(p.getClass(), answer, true);

						if(newPacket.getPacketHeader().getPacketError()==PacketErrors.NO_ERROR){
							final Integer value = Integer.valueOf(newPacket.getPayloads().get(0).getInt(2));
							if(list.size()==1){
								if(list.get(0).compareTo(value)<0)
									list.clear();
							}
							list.add(value);
						}
					}
				}catch(Exception e){
					logger.catching(e);
				}
			});
		}		
	}
}
