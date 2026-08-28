import * as serialPort from '../serial-port.js';
import packetId from '../packet/packet-properties/packet-id.mjs';
import groupId from '../packet/packet-properties/group-id.mjs';
import dd from '../packet/parameter/device-debug.js';
import { onStatusChange } from '../panel-summary-alarm.js';
import { onStartAll } from '../panel-info.js';
import CheckboxHelper from '../controller/helper/checkbox-helper.mjs'
import packetType from '../packet/packet-properties/packet-type.js'
import { intToBytes } from '../packet/service/converter.js';


const calModeDiv = document.querySelector('#calModeDiv');
export let calMode;

const options = {
    status: {
        on: 'Calibration Mode is ON',
        off: 'Calibration Mode is OFF',
    },
    hover: {
        on: 'Click to turn OFF.',
        off: 'Click to turn ON.',
    },
    css: {
        on: 'btn-info',
    }
}
const calModeHelper = new CheckboxHelper(document.querySelector('#cbCalMode'), onChange, options);

const action = {
    type: {
        code: packetType.request,
        name: 'request'
    },
    packetId: {
        code: packetId.calMode,
        name: 'calMode'
    },
    groupId: {
        code: groupId.deviceDebug,
        name: 'deviceDebug'
    },
    data: {
        codes: [{
            code: dd.calibrationMode.code,
            name: 'calibrationMode'
        }]
    },
    function: 'f_calMode',
    f_error: packetError
};

let hasError;
let interval;

onStartAll(yes => {
    if (yes) {
        if (!hasError)
            start();
    } else {
        stop();
    }
});


function start() {
    if (interval)
        return;

    calModeDiv.classList.remove('visually-hidden');

    run();
    interval = setInterval(run, 5000);
}


function stop() {
    clearInterval(interval);
    interval = undefined;
    hasError = false;
}


function run() {
    if (action.busy) {
        console.log('busy');
        return;
    }

    action.busy = true;
    serialPort.postObject(calModeDiv, action);
}


action.f_calMode = packet => {
    packet.payloads.forEach(pl => {
        if (pl.parameter.code !== dd.calibrationMode.code) {
            console.warn(pl);
            return;
        }

        calModeHelper.disabled = false;

		calMode = dd.calibrationMode.parser(pl.data);;
        calModeHelper.checked = calMode;
    });
};


const actionSet = {
    ...action,
	type: {
	    code: packetType.command,
	    name: 'command'
	},
	packetId: {
		code: packetId.calModeSet,
		name: 'calModeSet'
	},
	data: {
		...action.data
	}
};


function onChange({ value }) {
    actionSet.update = true;
    actionSet.data.values = {
		[dd.calibrationMode.code]: intToBytes(+value)};	// value - true/false

    serialPort.postObject(calModeDiv, actionSet);
}


onStatusChange(alarmStatus => {
    switch (alarmStatus.severities) {

        case 'Closed':
        case 'TIMEOUT':
            calModeHelper.disabled = true;
            break;

        case 'Stopped':
            stop();
            break;

        default:
            if (!hasError)
                start();
    }
});


function packetError(data) {
    if (data.error.packet?.header?.error === 10) { // Requested element not found
        console.warn(
            'The Packet has an error. Controller stops.\n',
           { packetStr: error.packet.toString(),
			action}
        );

        stop();
        hasError = true;
        calModeDiv.classList.add('visually-hidden');
    } else {
        console.warn(data);
    }
}