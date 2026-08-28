// panel-config.js
import * as serialPort from './serial-port.js'
import { type as unitType, onStartAll, onTypeChange } from './panel-info.js'
import ControlLoader from './helper/config-loader.js'

const $card = $('div.controlCard');

const action = { data: {}, function: 'f_Config' };

let controller;
let controller2;

let loader = new ControlLoader();
window.loader = loader;
let interval;
let busy;
const DELAY = 5000;

onStartAll(yes => {
    if (yes) {
        start();
        controller2?.start();
    } else {
        stop();
        controller2?.stop();
    }
})

let storedUnitType;
onTypeChange(type => {
    if (storedUnitType && (JSON.stringify(storedUnitType) === JSON.stringify(type)))
        return;
    if (controller) {
        controller.stop();
        controller.destroy();
        controller = null;
    }
    if (controller2) {
        controller2.stop();
        controller2.destroy();
        controller2 = null;
    }
    typeChange(type);
});
export function start() {
    if (interval || busy)
        return;

    busy = true;;
    action.busy = false;
    if (action.packetId) {
        clearInterval(interval);
        run()
        interval = setInterval(run, DELAY);
        controller?.start();
        controller2?.start();
    } else {
        if (unitType)
            typeChange(unitType);
    }
}

export function stop() {
    controller?.stop();
    controller2?.stop();
    interval = clearInterval(interval);
    busy = false;;
}

// panel-config.js
function typeChange(type) {
    if (storedUnitType && (JSON.stringify(storedUnitType) === JSON.stringify(type)))
        return;
    storedUnitType = type;

    loader.setUnitType(type, c => onControllerLoaded(c));

    switch (type.name) {
        case 'LNB':
        case 'CONTROLLER_ODRC':
            if (window.location.pathname === '/') {
                console.log('The user is not allowed to manage registers.');
                return;
            }
            if (type.revision > 30)
                import('./controller/controller-lnb-registers.js')
                    .then(({ default: Controller }) => {
                        controller2 = new Controller($card);
                    });
            //			interval = clearInterval(interval) ;
            break;

        case 'REFERENCE_BOARD':
            import('./controller/controller-dump-adc.js')
                .then(({ default: Controller }) => {
                    controller2 = new Controller($card);
                    controller2.start()
                });
            break;
        default:
            if (controller2) {
                controller2.destroy();
                controller2 = undefined;
            }
    }
}

// panel-config.js
function onControllerLoaded(Controller) {
    if (!Controller) {
        console.log('Controller is not ready.')
        return;
    }
    try {
        if (controller?.constructor.name !== Controller.name || controller?.parameter?.constructor.name !== loader.parameter.name) {
            controller = new Controller($card);
            controller.parametersClass = new loader.parameter();
            controller.change = onChange;
            action.packetId = loader.packetId
            action.groupId = controller.groupId
            actionSet = {
                ...action,
                data: {}
            };
            action.update = true;
        }

        busy = false;

        start();

    } catch (error) {
        console.error('Failed to initialize controller.', error);
    }
}

function run() {
    if (!serialPort.doRun()) {
        stop();
        return;
    }

    if (action.busy || !controller?.parametersClass) {
        console.log(action.busy ? 'busy' : 'No data to send');
        return
    }

    const toRead = controller.toRead;
    if (!toRead?.length) {
        console.warn('No data to Read')
        return;
    }

    if (action.data.parameterCode?.toString() !== toRead?.toString()) {
        action.update = true;
        action.data.parameterCode = toRead;
    }
    if (action.data.parameterCode.length) {
        serialPort.postObject($card, action);
    } else
        stop();
}

action.f_Config = function(packet) {
    if (!controller)
        return;

    controller.update = packet.payloads;
}

action.f_error = error => {
    console.error({ action, error });
}

let actionSet;
function onChange(packetId, value, parameterCode, groupId) {

    // new style code
    if (value == null && parameterCode == null) {
        const a = { ...actionSet, ...packetId };

        serialPort.postObject($card, a);
        return;
    }

	// deprecated
    actionSet.update = true;
    actionSet.groupId = groupId ?? controller.groupId
    actionSet.packetId = packetId;
    actionSet.data.value = value;
    actionSet.data.parameterCode = parameterCode;
    actionSet.command = true;
    serialPort.postObject($card, actionSet);
}

export function update(object) {
    controller.update = object;
}
