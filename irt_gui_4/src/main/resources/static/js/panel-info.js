// panel-info.js

import * as serialPort from './serial-port.js';
import packetId from './packet/packet-properties/packet-id.mjs';
import groupId from './packet/packet-properties/group-id.mjs';
import { onStatusChange } from './panel-summary-alarm.js';
import f_deviceType from './packet/service/device-type.js';
import translator from './helper/text-translator.mjs';

const card = document.querySelector('.infoCard');
const body = document.querySelector('.info');

export let type;
let serialNumber

const map = new Map();
const parameter = {};

let stopCalled = false;
let interval;
const action = {packetId: packetId.deviceInfo, groupId: groupId.deviceInfo, data: {}, function: 'f_Info'};

onStatusChange(statusChange);

function statusChange(alarmStatus) {
    const doRun = alarmStatus.index !== 7 && alarmStatus.index !== 8 && alarmStatus.index !== 11;
    if (doRun && serialPort.doRun()) {
        start();
    } else {
        stop();
    }
}

serialPort.onStart(onStart);

function onStart(doRun) {
    if (doRun) {
        console.log('start');
    } else {
        stop();
    }
}

let emptyCard;
export function start() {
    emptyCard = card.querySelector('.placeholder') !== null;
    
    if (interval) return;
    
    action.busy = false;
    getParameter();
}

export function stop() {
    if (interval) {
        clearInterval(interval);
        interval = null;
    }
    stopAll();
}

const typeChangeEvents = new Set();
export function onTypeChange(e) {
    typeChangeEvents.add(e);
}

const serialNumberChangeEvents = new Set();
export function onSerialChange(e) {
    serialNumberChangeEvents.add(e);
}

async function getParameter() {
    if (!parameter.parser) {
        const { default: deviceInfo, description, comparator, parser } = await import('./packet/parameter/device-info.js');
        parameter.deviceInfo = deviceInfo;
        parameter.parser = parser;
        parameter.description = description;
        parameter.comparator = comparator;
        action.data.parameterCode = parameter.deviceInfo.all;
    }
    run();
    if (interval) {
        clearInterval(interval);
    }
    interval = setInterval(run, 5000);
}

function run() {
    if (!serialPort.doRun()) {
        stop();
        return;
    }
    
    if (action.busy) {
        console.log('busy');
        return;
    }
    
    action.busy = true;
    serialPort.postObject(card, action);
}

action.f_Info = function(packet) {
    const payloads = packet.payloads;
    
    let timeout;
    if (!map.size) {
        payloads.sort(parameter.comparator);
    }
    
    payloads.forEach(pl => {
        const parameterCode = pl.parameter.code;
        const row = map.get(parameterCode);
        const valId = 'infoVal' + parameterCode;
        const descrId = 'infoDescr' + parameterCode;
        const parser = parameter.parser(parameterCode);
        
        if (!parser) {
            console.warn('No parser. (Parameter code: )' + parameterCode);
            return;
        }
        
        const val = parser(pl.data);
        if (!val) {
            console.warn('No value. ', pl);
            return;
        }
        
        if (row) {
            const valElement = row.querySelector('#' + valId);
            switch (parameterCode) {
                case parameter.deviceInfo.type:
                    if (!type || val[0] !== type.type || val[1] !== type.revision || val[2] !== type.subtype) {
                        valElement.textContent = val;
                    }
                    break;
                default:
                    if (val !== valElement.textContent) {
                        valElement.textContent = val;
                    }
            }
        } else {
            if (emptyCard) {
                emptyCard = false;
                body.innerHTML = '';
            }
            
            const showText = parameter.description(parameterCode);
            const row = document.createElement('div');
            row.className = 'row';
            
            let valueElement;
            if (showText && parameterCode !== parameter.deviceInfo.description) {
                const descElement = document.createElement('div');
                descElement.id = descrId;
                descElement.className = 'col-5';
                descElement.textContent = showText;
                row.appendChild(descElement);
                
                if (parameterCode === 5) { // Serial Number
                    valueElement = document.createElement('div');
                    valueElement.className = 'col';
                    const link = document.createElement('a');
                    link.id = valId;
                    link.textContent = val;
                    link.target = '_blank';
                    link.href = `http://${val}`;
                    valueElement.appendChild(link);
                } else {
                    valueElement = document.createElement('div');
                    valueElement.id = valId;
                    valueElement.className = 'col';
                    valueElement.textContent = val;
                }
            } else {
                valueElement = document.createElement('div');
                valueElement.id = descrId;
                valueElement.className = 'col';
                const heading = document.createElement('h4');
                heading.id = valId;
                heading.textContent = val;
                valueElement.appendChild(heading);
            }
            
            row.appendChild(valueElement);
            map.set(parameterCode, row);
            
            clearTimeout(timeout);
            timeout = setTimeout(() => {
                const fragment = document.createDocumentFragment();
                map.forEach(value => fragment.appendChild(value));
                body.appendChild(fragment);
            }, 100);
        }
        
        switch (parameterCode) {
            case parameter.deviceInfo.type:
                const spl = val.split('.').map(v => +v);
                if (type?.type !== spl[0] || type.revision !== spl[1] || type.subtype !== spl[2]) {
                    changeType(spl);
                } else if (stopCalled) {
                    startAll();
                }
                break;
                
            case parameter.deviceInfo.serialNumber:
                if (serialNumber !== val) {
                    serialNumber = val;
                    import('./fw-upgrade.js').then(m => m.setSerialNumber(val));
                    serialNumberChangeEvents.forEach(cb => cb(val));
                    if (row) {
                        const link = row.querySelector(`#${valId}`);
                        if (link) link.href = `http://${val}`;
                    }
                }
                break;
                
            case parameter.deviceInfo.serialNumber:
                const split = document.title.split(' - ');
                if (val !== split[0]) {
                    document.title = val + ' - ' + split[1];
                }
        }
    });
}

function changeType(val) {
    type = { name: f_deviceType(val[0]), type: val[0], revision: val[1], subtype: val[2] };
    startAll();
    typeChangeEvents.forEach(e => e(type));
}

const onStartEvent = [];
export function onStartAll(cb) {
    onStartEvent.push(cb);
}

function startAll() {
    stopCalled = false;
    onStartEvent.forEach(cb => cb(true));
}

function stopAll() {
    stopCalled = true;
    onStartEvent.forEach(cb => cb(false));
}

export function profileSearch(cb) {
    if (!serialNumber) {
        setTimeout(() => getProfilePath(cb), 100);
        return;
    }
    getProfilePath(cb);
}

function getProfilePath(cb) {
    if (!serialNumber) {
        cb({ warn: 'No serial number' });
        return;
    }
    
    const url = new URL('/file/path/profile', window.location.origin);
    url.searchParams.append('sn', serialNumber);
    
    fetch(url)
        .then(response => response.json())
        .then(data => {
            cb({ path: data });
        })
        .catch(error => {
            console.error('Error fetching profile path:', error);
            cb({ error: error.message });
        });
}