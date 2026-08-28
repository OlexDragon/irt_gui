
import * as serialPort from './serial-port.js'
import groupId from './packet/packet-properties/group-id.mjs'
import packetId from './packet/packet-properties/packet-id.mjs'
import { controlBuc } from './packet/parameter/config-buc.mjs'
import { type as unitType } from './panel-info.js'
import packetType from './packet/packet-properties/packet-type.js'
import SelectHelper from './controller/helper/select-helper.mjs'
import { translate } from './packet/service/converter.js'

const parameters = controlBuc.parameters;

const card = document.querySelector('#userCard')
const body = document.querySelector('#redundancy-tab-pane')

let redundancyImg
let redOnline
let redundancyStatus

const controllers = {};

const action = {
    type: {
        code: packetType.request,
        name: 'request'
    },
    packetId: {
        code: packetId.redundancyAll,
        name: 'redundancyAll'
    },
    groupId: {
        code: groupId.configuration,
        name: 'configuration'
    },
    data: {
        codes: [
            parameters.redEnable,
            parameters.redName,
            parameters.redMode,
            parameters.redStatus
        ]
    },
    function: 'f_Redundancy'
};

let interval
const delay = 5000;
let fragmentName;

export async function start() {
    if (interval)
        return;

    action.busy = false;

    const name = chooseFragmentName();

    try {
        if (name !== fragmentName) {

            const response = await fetch(`/fragment/redundancy/${name}`);

            if (!response.ok)
                throw new Error(`Failed to load redundancy fragment: ${response.status}`);

            fragmentName = name;

            body.innerHTML = await response.text();

            controllers.redEnable = new SelectHelper(body.querySelector('#redEnable'), onSendCommand);
            controllers.redMode = new SelectHelper(body.querySelector('#redMode'), onSendCommand);
            controllers.redName = new SelectHelper(body.querySelector('#redName'), onSendCommand);

            redundancyImg = body.querySelector('#redundancyImg');
            redundancyStatus = body.querySelector('#redundancyStatus');

            redOnline = body.querySelector('#redOnline');
            redOnline.addEventListener('click', onClick);
        }

        clearInterval(interval)
        run();
        interval = setInterval(run, delay)
    } catch (error) {
        console.error('Failed to load redundancy fragment.', error)
    }
}

export function stop() {
    clearInterval(interval)
    interval = undefined
}

export function disable() {

    Object.values(controllers).forEach(controller => {
        controller.disabled = true;
    });

    redOnline.disabled = true;
}

function chooseFragmentName() {
    switch (unitType?.name) {
        default:
            return 'buc'
    }
}

function run() {
    if (!serialPort.doRun()) {
        stop()
        return
    }

    if (action.busy) {
        console.log('busy')
        return
    }

    action.busy = true

    serialPort.postObject(card, action)
}

action.f_Redundancy = function(packet) {
    const payloads = packet.payloads

    if (!payloads?.length) {
        console.log(packet.toString())
        console.warn('No payloads to parse.')
        blink(card, 'connection-wrong')
        return
    }

    payloads.forEach(parse)
}

function parse(pl) {
    const code = pl.parameter.code
    const value = controlBuc.parser(code)(pl.data)
    const name = controlBuc.toName(code)

    const controller = controllers[name]

    if (controller) {
        controller.value = value
        controller.disabled = false
    }

    if (code === parameters.redStatus.code) {
        updateStatus(value)
    }
}

function updateStatus(status) {
    if (redundancyStatus.textContent !== status.text)
        redundancyStatus.textContent = status.text ? translate('redundancy.status', status.text) : '';

    redOnline.disabled = !status.isStandby;

    const online = status.isOnline;
    Object.values(controllers).forEach(controller => {
        controller.disabled = !online;
    });

    const redundancyEnabled = controllers.redEnable.value === 'true';

    if (!status.isStandby && !online && redundancyEnabled)
        controllers.redEnable.disabled = false;

    updateImage(status);
}

const imageLinks = [
    '/images/BUC_X.jpg',
    '/images/BUC_A.jpg',
    '/images/BUC_B.jpg'
]
function updateImage(status) {
    const nameCode = Number(controllers.redName.value)

    if (!status.isOnline && !status.isStandby) {
        redundancyImg.src = imageLinks[0]
        return
    }

    const imageIndex = status.value === nameCode ? 1 : 2
    redundancyImg.src = imageLinks[imageIndex]
}

function onClick({ currentTarget: { id, value } }) {
    onSendCommand({ id, value });
}

const actionSet = {
    ...action,
    type: {
        code: packetType.command,
        name: 'command'
    },
    packetId: {
        code: packetId.redundancySet,
        name: 'redundancySet'
    }
};

function onSendCommand({ id, value }) {

    actionSet.update = true;

    const data = {
        values: {},
        raw: value
    };
    actionSet.data = data;

    switch (id) {
        case 'redOnline':
            data.codes = [parameters.redOnline];// There is no need to send a value.
            break

        case 'redEnable': {
            const obj = parameters.redEnable;
            data.codes = [obj];
            data.values[obj.code] = [value === 'true' ? 1 : 0];
        }
            break

        default: {

            const obj = parameters[id];
            if (!obj) {
                console.warn(id + '- Invalid element properties, needs to be renamed.')
                return
            }

            data.codes = [obj];
            data.values[obj.code] = [Number(value)];
        }
    }

    serialPort.postObject(card, actionSet)
}
