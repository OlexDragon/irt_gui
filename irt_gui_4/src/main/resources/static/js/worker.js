import { onStatusChange } from './panel-summary-alarm.js'
import { start, stop} from './panel-info.js'
import * as serialPort from './serial-port.js'

onStatusChange(onStart);

function onStart(alarmStatus){
	const doRun = alarmStatus.index !== 7 && alarmStatus.index !== 8;
	if(doRun && serialPort.doRun())
		start();
	else{
		stop();
	}
}
