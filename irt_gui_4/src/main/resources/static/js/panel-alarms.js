import * as serialPort from './serial-port.js'
import groupId from './packet/packet-properties/group-id.mjs'
import packetId from './packet/packet-properties/packet-id.mjs'
import { code, parser } from './packet/parameter/alarm.js'
import { onTypeChange } from './panel-info.js'
import translator from './helper/text-translator.mjs';

const card = document.getElementById('userCard');
const body = document.getElementById('alarms-tab-pane');
const codeIdIDs = code('IDs');
const codeIdDescription = code('description');
const codeIdStatus = code('status');

const action = { name: 'panel-alarms', packetId: packetId.alarmIDs, groupId: groupId.alarm, data: { parameterCode: codeIdIDs }, function: 'f_Alarms' };
onTypeChange(() => {
    action.IDs = undefined;
    action.packetId = packetId.alarmIDs
    action.data.parameterCode = codeIdIDs;
    readAlarmDescription = true;
    descriptionIndex = 0;
    body.innerHTML = '';
    map.clear();
})

let interval;
let delay = 5000;
const map = new Map();

export function start() {

    if (interval)
        return;

    action.busy = false;
    stop();
    run();
    interval = setInterval(run, delay);
}

export function stop() {
    clearInterval(interval);
    interval = undefined;
}

let readAlarmDescription = true;
function run() {
    if (!serialPort.doRun()) {
        stop();
        return;
    }

    if (action.busy) {
        console.warn('action.busy');
        return
    }

    action.busy = true;

    if (action.IDs) {

        if (readAlarmDescription)
            getAlarmDescription();
        else
            serialPort.postObject(card, action);

    } else {

        serialPort.postObject(card, action);
    }

}

let alarmIndex;
action.f_Alarms = function(packet) {
    alarmIndex = -1;
    packet.payloads.forEach(parseAlarm);
}

function parseAlarm(pl) {

    switch (pl.parameter.code) {
        case codeIdIDs:

            action.IDs = action.data.value =
                parser(pl.parameter.code)(pl.data);

            action.packetId = packetId.alarmDescription;
            action.data.parameterCode = codeIdDescription;

            getAlarmDescription();

            break;

        case codeIdDescription:
            showDescription(pl);
            getAlarmDescription();
            break;

        case codeIdStatus:
            showValue(pl);
            break;

        default:
            console.warn(pl);
    }
}

function showValue(pl) {
    const p = parser(pl.parameter.code);
    if (!p) {
        console.warn('Parser not found')
        return;
    }
    const value = p(pl.data);
    const row = getRow(value.id);
    const div = row.querySelector('.value');
    if (div.textContent !== value.text)
        div.textContent = value.text;
    if (!div.classList.contains(value.boorstrapClass))
        removeClasses(div).classList.add('col', value.boorstrapClass)

    if (value.index > alarmIndex)
        alarmIndex = value.index;
}

function showDescription(pl) {
    const p = parser(pl.parameter.code);
    if (!p) {
        console.warn('Parser not found')
        return;
    }
    const value = p(pl.data);
    const row = getRow(value.id);
    const div = row.querySelector('.name');
	const text = translator.translate(value.string); // In this case, the alarm name came from the device.
    if (div.textContent !== text)
        div.textContent = text;
}

let timeout;
function getRow(id) {
    let row = map.get(id);
    if (!row) {
        row = document.createElement('div');
        row.id = 'row' + id;
        row.className = 'row mt-1';

        const nameDiv = document.createElement('div');
        nameDiv.className = 'col name text-end fw-bold';
        row.appendChild(nameDiv);

        const valueDiv = document.createElement('div');
        valueDiv.className = 'col value text-center fs-6';
        row.appendChild(valueDiv);

        map.set(id, row);
        clearTimeout(timeout);
        timeout = setTimeout(() => {
            const fragment = document.createDocumentFragment();
            for (const row of map.values()) {
                fragment.appendChild(row);
            }
            body.appendChild(fragment);
        }, 100);
    }
    return row;
}

function removeClasses(el) {
    const classes = el.className.split(' ').filter(c => c.startsWith('text-bg-'));
    el.classList.remove(...classes);
    return el;
}

let descriptionIndex = 0;
function getAlarmDescription() {
    if (descriptionIndex >= action.IDs.length) {
        action.packetId = packetId.alarm;
        action.data.parameterCode = codeIdStatus;
        action.data.value = action.IDs;
        readAlarmDescription = false;
        run();
        return;
    }
    action.update = true;

    action.data.value = [action.IDs[descriptionIndex]];
    ++descriptionIndex;
    serialPort.postObject(card, action);

}