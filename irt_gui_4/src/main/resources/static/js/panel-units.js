// panel-units.js
// Panel Units Selection - allows selecting different units/modules in a panel device.
// Connection via controller

import * as serialPort from './serial-port.js';
import { info as idInfo } from './packet/packet-properties/packet-id.mjs';
import { info as groupInfo } from './packet/packet-properties/group-id.mjs';
import { onTypeChange } from './panel-info.js';
import ModuleSelector from './helper/module-selector.mjs';
import { info as typeInfo } from './packet/packet-properties/packet-type.js'

const unitsSelect = document.querySelector('#unitsSelect');
let parser;

const action = {
    type: typeInfo.request,
    packetId: idInfo.module,
    groupId: groupInfo.control,
    data: {},
    function: 'f_modules'
};
const moduleEvents = new Set();

export function onModules(callback) {
    moduleEvents.add(callback);
}

export const moduleSelector = new ModuleSelector(unitsSelect);
moduleSelector.onChange(value => sendCommand(value));

export function select(value, callBack) {
    sendCommand(value, callBack);
}

let commandCallback;
function sendCommand(value, callBack) {

    if (!actionSet.data.codes) {
        console.warn('This operation cannot be performed.', actionSet);
        callBack?.({ message: 'This operation cannot be performed.', actionSet })
        return;
    }

    commandCallback = callBack;
    setcommandPending(false);

    actionSet.data.values = { [parameter.config.activeModule]: [value] };

    actionSet.update = true;
    serialPort.postObject(unitsSelect, actionSet);

    setcommandPending(true);

}

function setcommandPending(disable) {
    commandPending = disable;
    moduleSelector.disable(disable);
}
const changeEvents = new Set();
const parameter = {};

let interval;
let previousType;
let commandPending;

serialPort.onStart(startStop);

function startStop(yes) {
    yes ? start() : stop();
}


onTypeChange(t => {

    if (previousType?.type == t.type && previousType?.subtype === t.subtype)
        return;

    previousType = t;
    delete action.data.codes;

    resetModules();
    start();
});

export function start() {
    if (interval)
        return;

    action.busy = false;
    action.packetError = undefined;

    switch (previousType?.name) {
        case 'CONVERTER':
            return;
    }

    getParser();
}

export function stop() {
    interval = clearInterval(interval);
}

export function disable(disabled) {
    moduleSelector.disable(disabled);
}

export function onChange(callback) {
    changeEvents.add(callback);
}

export function getModules() {
    return moduleSelector.modules();
}

function getParser() {
    if (parameter.parser)
        return requestModuleList();

    import('./packet/parameter/config-modules.js')
        .then(({ default: config, info, parser }) => {
            parameter.config = config;
            parameter.info = info;
            parameter.parser = parser;

            requestModuleList();
        });
}

function requestModuleList() {
    action.data.codes = [parameter.info.moduleList];
    parser = parameter.parser(parameter.config.moduleList)
    action.update = true;

    restart();
}

function restart() {
    stop();
    run();
    interval = setInterval(run, 10000);
}

function run() {
    if (action.packetError) {
        resetModules();// The module may be disabled, wait for a response.
        return;
    }

    if (!serialPort.doRun()) {
        stop();
        return;
    }

    if (action.busy) {
        console.log('busy');
        return;
    }

    action.busy = true; // serialPort sets action.busy = false even when action.f_modules is not called

    serialPort.postObject(unitsSelect, action);
}

action.f_modules = packet => {
    packet.payloads.forEach(pl => {
        const code = pl.parameter.code;
        const value = parser(pl.data);

        switch (code) {
            case parameter.config.moduleList:
                if (Object.keys(value).length < 2) {
                    resetModules();
                    return;
                }

                moduleSelector.setModules(value);
                moduleEvents.forEach(callback => callback(value));

                actionSet.data.codes = action.data.codes = [parameter.info.activeModule];
                parser = parameter.parser(parameter.config.activeModule)
                action.update = true;

                restart();
                break;

            case parameter.config.activeModule:
                if (!moduleSelector.setSelected(value))
                    break;

                commandCallback?.({
                    value,
                    actionSet
                });
                commandCallback = undefined

                if (commandPending) {	// to callback once
                    setcommandPending(false);
                    changeEvents.forEach(callback => callback());
                }
                break;

            default:
                console.warn(packet.toString());
        }
    });
};

action.f_error = (packet) => {
    setcommandPending(false);
    console.warn(packet);
}
const actionSet = {
    ...action,
    type: typeInfo.command,
    packetId: idInfo.moduleSet,
    data: {}
};

function resetModules() {
    stop();
    moduleSelector.clear();
}