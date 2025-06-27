import * as serialPort from './serial-port.js'
import Register from './classes/register.js'
import packetId from './packet/packet-properties/packet-id.js'

const reg1 = new Register('reg1', $('#reg1Card'), packetId.register1, packetId.registerSet);
const reg2 = new Register('reg2', $('#reg2Card'), packetId.register2, packetId.registerSet);
const reg3 = new Register('reg3', $('#reg3Card'), packetId.register3, packetId.registerSet);

//$min.change();
//$max.change();

serialPort.onStart(onStart);

let interval;
function onStart(doRun){
	if(doRun){
		run();
		interval = setInterval(run, 3000);
	}else
		interval = clearInterval(interval);
}

function run(){
	const ready1 = reg1.ready();
	const ready2 = reg1.ready();
	const ready3 = reg1.ready();

	if(!(ready1 || ready2 || ready3)){
		serialPort.showToast('Fields must be filled in.', 'It is necessary to fill in the "Index" and "Address" fields.', 'text-bg-warning bg-opacity-50')
		return;
	}
	if(ready1)
		reg1.send();
	if(ready2)
		reg2.send();
	if(ready3)
		reg3.send();
}
