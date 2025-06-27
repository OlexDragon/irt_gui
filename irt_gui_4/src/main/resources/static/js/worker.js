import { onStatusChange } from './panel-summary-alarm.js'
import { start, stop} from './panel-info.js'
import * as serialPort from './serial-port.js'

onStatusChange(onStart);

function onStart(doRun){
	if(doRun && serialPort.doRun())
		start();
	else{
		stop();
	}
}
