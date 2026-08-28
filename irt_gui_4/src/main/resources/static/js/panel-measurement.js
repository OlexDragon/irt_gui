import * as serialPort from './serial-port.js';
import { onTypeChange, onStartAll } from './panel-info.js';
import MeasurementLoader from './helper/measurement-loader.js';
import groupId from './packet/packet-properties/group-id.mjs';

const card = document.querySelector('.measurementCard');
const body = document.querySelector('.measurement');

const action = {
    data: {},
    function: 'f_measurement',
    groupId: groupId.measurement
};

let controller;
const loader = new MeasurementLoader();

let interval;
let busy = false;
let hasPlaceholder = true;

onStartAll(startAll => startAll ? start() : stop());
onTypeChange(typeChange);

export function start() {
    if (interval || busy)
        return;

    busy = true;
    action.busy = false;

    if (!action.packetId)
        return;

    run();
    interval = setInterval(run, 3000);
}

export function stop() {
    clearInterval(interval);
    interval = undefined;
    busy = false;
}

function typeChange(type) {
    loader.setUnitType(type, onControllerLoaded);
}

function onControllerLoaded(Controller) {
    if (!Controller) {
        console.log('The Controller is not ready.');
        return;
    }

    const controllerChanged =
        controller?.constructor.name !== Controller.name ||
        controller?.parameter.constructor.name !== loader.parameter.name;

    if (controllerChanged) {
        if (!hasPlaceholder)
            body.replaceChildren();

        controller = new Controller($(card));
        controller.parametersClass = new loader.parameter();

        action.packetId = loader.packetId;
        action.groupId = controller.groupId;
        action.data.parameterCode = 255;
    }

    busy = false;
    start();
}

function run() {
    if (!serialPort.doRun()) {
        stop();
        return;
    }

    if (action.busy) {
        console.log('Busy');
        return;
    }

    action.busy = true;
    serialPort.postObject(card, action);
}

action.f_measurement = packet => {
    if (hasPlaceholder) {
        if (card.querySelector('.placeholder'))
            body.replaceChildren();

        hasPlaceholder = false;
    }

    controller && (controller.update = packet.payloads);
};