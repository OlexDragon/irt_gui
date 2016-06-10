package irt.gui.controllers.calibration.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Optional;

import irt.gui.controllers.calibration.tools.Tool;
import irt.gui.controllers.calibration.tools.Tool.Commands;
import irt.gui.data.packet.interfaces.PacketToSend;

public class AverageDouble extends Average<Double> {

	public AverageDouble(Tool tool, Commands commands) {
		super(tool, commands);
	}

	@Override
	public Double call() throws Exception {

		List<Double> list = new ArrayList<>();
		observer.setList(list);

		Double a1 = Double.MAX_VALUE;
		Double a2 = Double.MIN_VALUE;

		while((list.size()<=5 || a1.compareTo(a2)!=0) && list.size()<100){
			a1 = a2;

			synchronized (this) {
				try {

					tool.get(command, observer);

					wait(100);

				} catch (Exception e) {
					logger.catching(e);
					break;
				}
			}

			final Double average = getAverage(list);
			logger.trace("old:{}; new:{}", a2, average);
			a2 = average;
		}

		logger.debug("average of {}", list);
		return getAverage(list);
	}

	private Double getAverage(List<Double> list) {
		return new ArrayList<>(list)
				.parallelStream()
				.mapToDouble(d->d)
				.average()
				.orElse(0.0);
	}

	private final ValueObserver observer = new ValueObserver();

	///****************************** class ValueObserver   ****************************
	private class ValueObserver implements Observer{

		private List<Double> list;

		public void setList(List<Double> list) {
			this.list= list;
		}

		@Override
		public void update(Observable o, Object arg) {
			EXECUTOR.execute(()->{
				o.deleteObserver(this);
				try{
					final PacketToSend p = (PacketToSend)o;
					final byte[] answer = p.getAnswer();
					Optional
					.ofNullable(answer)
					.map(String::new)
					.map(String::trim)
					.map(s->s.split("\n"))
					.map(split->Arrays
						.stream(split)
						.filter(s->Character.isDigit(s.charAt(s.length()-1)))
						.reduce((a, b) -> b)	//find last value
						.orElse(null))
					.map(Double::parseDouble)
					.ifPresent(d->{
						logger.trace("value: {}; list: {}", d, list);
						if(list.size()==1){
							if(list.get(0).compareTo(d)<0)
								list.clear();
						}
						list.add(d);
					});

					if(list.size()==2 && Double.compare(list.get(0), list.get(1))<0)
						list.remove(0);

				}catch(Exception e){
					logger.catching(e);
				}
			});
		}		
	}
}
