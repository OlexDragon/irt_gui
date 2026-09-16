// serial-port.js

import f_toSend from './to-send.js'
import Packet from './packet/packet.js'
import UnitAddress from './classes/unit-address.js'
import { start as summaryAlarmStart, stop as summaryAlarmStop, textToStatus, closed, onStatusChange } from './panel-summary-alarm.js'
import { status as f_alarmStatus } from './packet/parameter/value/alarm-status.js'
import packetType from './packet/packet-properties/packet-type.js'
import CslSerialPort from './serial/csl-serial-port.mjs';

const cslSerialPort = new CslSerialPort({

    serialPortElement: document.getElementById('serialPort'),
    baudrateElement: document.getElementById('baudrate'),
    exitElement: document.getElementById('appExit'),
    modal: document.getElementById('modal'),
    callback: portSelected,
    storageKey: 'gui',
    beforeExit: () => {
        summaryAlarmStop();
        btnStartEvents.forEach(cb => cb(false));
    }
});

export let showError;
export const baudrate = cslSerialPort.baudrate;
const selectSerialPort = cslSerialPort.serialPort
const unitAddressElement = document.getElementById('unitAddress');
export const unitAddrClass = new UnitAddress(unitAddressElement);

// DOM element references

const btnStart = document.getElementById('btnStart');
const toastContainer = document.getElementById('toastContainer');
const coverDiv = document.getElementById('cover');
const btnShowErrors = document.getElementById('btnShowErrors');
const appExit = document.getElementById('appExit');
const summaryAlarmCard = document.getElementById('summaryAlarmCard');

export function doRun() {
    return btnStart.checked;
}

const btnStartEvents = [];

export function onStart(cb) {
    btnStartEvents.push(cb);
}
export function removeOnStart(cb) {
    const index = btnStartEvents.indexOf(cb);
    if (index >= 0)
        btnStartEvents.splice(index, 1);
}

export function postObject(card, action) {
    f_toSend(action, toSend => send(card, toSend, action));
}

export function blink(el, bootstrapClass = 'connection-ok') {
    // Temporary compatibility while both DOM and jQuery are used.
    if (el.length && !el.classList)
        el = el[0];

    el.classList.add(bootstrapClass);
    setTimeout(() => el.classList.remove(bootstrapClass), 1000);
}

export function showToast(title, message, headerClass) {
    const toast = document.createElement('div');
    toast.className = 'toast';
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'assertive');
    toast.setAttribute('aria-atomic', 'true');

    const header = document.createElement('div');
    header.className = 'toast-header';

    const strong = document.createElement('strong');
    strong.className = 'me-auto';
    strong.textContent = title;
    header.appendChild(strong);

    const closeBtn = document.createElement('button');
    closeBtn.className = 'btn-close';
    closeBtn.type = 'button';
    closeBtn.setAttribute('data-bs-dismiss', 'toast');
    closeBtn.setAttribute('aria-label', 'Close');
    closeBtn.addEventListener('click', function() {
        const bsToast = bootstrap.Toast.getInstance(toast);
        if (bsToast) bsToast.hide();
    });
    header.appendChild(closeBtn);
    toast.appendChild(header);

    const body = document.createElement('div');
    body.className = 'toast-body';
    body.textContent = message;
    toast.appendChild(body);

    addClasses(header, headerClass);

    toast.addEventListener('hide.bs.toast', function() {
        this.remove();
    });

    toastContainer.appendChild(toast);
    new bootstrap.Toast(toast).show();
}

function addClasses(element, classes) {
    if (!classes) return;
    element.classList.add(...classes.trim().split(/\s+/));
}

export function stop() {
    if (btnStart.checked)
        btnStart.click();
}

// Event listeners
btnStart.addEventListener('change', toggleStart);
btnShowErrors.addEventListener('change', btnShowErrorsChange);

(() => {
    showError = Cookies.get('btnShowErrors') === 'true';
    btnShowErrors.checked = showError;
})();

export let serialPort;
function portSelected(data) {
    coverButSerial(false);
    if (!data?.value) {
        console.warn('Midding data')
        returm;
    }
    serialPort = data.value;
    btnStart.disabled = false;
    toggleStart();
}

async function toggleStart() {

    const lbl = btnStart.nextElementSibling;
    const text = lbl.textContent;

    switch (text) {
        case txtStart:
            btnStartEvents.forEach(cb => cb(true));
            summaryAlarmStart();
            lbl.textContent = txtStop;
            btnStart.checked = true;
            break;

        default:
            btnStartEvents.forEach(cb => cb(false));
            summaryAlarmStop();
            lbl.textContent = txtStart;
            btnStart.checked = false;

            if (closed())
                return;

            try {
                const response = await fetch('/serial/close', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams({ spName: serialPort })
                });

                const result = await response.json();

                if (result) {
                    console.log('Serial port closed successfully');
                } else {
                    console.log('Failed to close serial port');
                }
            } catch (error) {
                console.error('Error closing serial port:', error);
            }
            textToStatus('Stopped:The program has stopped accessing the serial port.')
    }
}

function btnShowErrorsChange(e) {
    showError = e.currentTarget.checked;
    Cookies.set('btnShowErrors', showError, { expires: 365, path: '' });
    if (showError)
        showToast('Display of error messages is enabled.', 'Error information will be displayed here..');
}

function send(card, toSend, action) {

    if (!btnStart.checked) {
        console.warn('Stop button pressed, unauthorized attempt to use serial port.', action);
        return;
    }

    if (!toSend?.bytes) {
        console.warn('No data to send.', toSend, action);
        blink(card, 'connection-wrong');
        return;
    }
    const json = JSON.stringify(toSend);
    async function sendSerialRequest() {
        try {
            const response = await fetch('/serial/send', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: json
            });

            if (!response.ok) {
                try {
                    const errorData = await response.json();
                    action.onFail?.({
                        response,
                        error: null
                    });
                    blink(card, 'connection-fail');

                    console.log(response.headers);

                    if (errorData?.message) {
                        if (showError) {
                            showToast(errorData.error, errorData.message, 'text-bg-danger bg-opacity-50');
                        }
                    } else {
                        textToStatus('Closed:The application is not responding.');
                    }
                } catch {
                    action.onFail?.({
                        response,
                        error: null
                    });
                    blink(card, 'connection-fail');

                    console.log(response.headers);
                    textToStatus('Closed:The application is not responding.');
                }
                return;
            }

            const data = await response.json();

            if (data.error) {
                action.packetError = data.error;
                textToStatus(data.error);
                onError({
                    message: 'No answer.',
                    data
                }, action);
                return;
            }

            if (!data.answer?.length) {
                blink(card, 'connection-wrong');
                onError({
                    message: 'No answer.',
                    data
                }, action);
                return;
            }

            if (!data.function) {
                onError({
                    message: 'No function name.',
                    data
                }, action);
                return;
            }

            const packet = new Packet(data.answer, data.unitAddr);

            const id = action.packetId.code ?? action.packetId;
            if (id !== packet.header.packetId) {
                blink(card, 'connection-wrong');
                onError({
                    message: 'Received wrong packet.',
                    packet
                }, action);
                return;
            }

            if (packet.header.error) {
                const packetStr = packet.toString();
                blink(card, 'connection-wrong');
                if (showError)
                    showToast('Packet Error', packetStr, 'text-bg-danger bg-opacity-50');

                action.packetError = packet.header.toString();
                onError({
                    message: 'Packet Error',
                    packet
                }, action);
                return;
            }

            if (!(packet.payloads?.length || packet.header.groupId === packetType.acknowledgement)) {
                if (packet.header.type !== packetType.acknowledgement) {
                    console.log(action, packet);
                    console.warn('Packet does not have payloads.');
                }
                blink(card, 'connection-wrong');
                onError({
                    message: 'Packet does not have payloads.',
                    data
                }, action);
                return;
            }

            blink(card);
            action[data.function](packet);

        } catch (error) {
            action.onFail?.({
                response: null,
                error
            });
            console.error(error, { action });
            blink(card, 'connection-fail');
            textToStatus('Closed:The application is not responding.');
        } finally {
            action.busy = false;
        }
    }

    return sendSerialRequest();
}

function onError(error, action) {
    if (action.f_error)
        action.f_error({ error, action });
    else
        console.warn({ error, action });

}

function coverButSerial(cover) {
    coverDiv?.classList.toggle('cover', cover);
    selectSerialPort.toggleClass('to-front', cover);
    appExit.classList.toggle('to-front', cover);
}

onStatusChange(s => {
    const index = ((s.severities === 'TIMEOUT' || s.severities === 'SP Error' || s.severities === 'Stopped') ? 1 : 0) + (s.severities === 'Closed' ? 2 : 0)
    switch (index) {
        case 1:
            btnStart.nextElementSibling.classList.add('to-front');
        case 2:
            coverDiv?.classList.add('cover');
            selectSerialPort.toggleClass('to-front', true);
            baudrate.toggleClass('to-front', true);
            unitAddressElement.classList.add('to-front');
            if (unitAddressElement.nextElementSibling) {
                unitAddressElement.nextElementSibling.classList.add('to-front');
            }
            summaryAlarmCard.classList.add('to-front');
            appExit.classList.add('to-front');
            break;

        default:
            coverDiv?.classList.remove('cover');
            selectSerialPort.toggleClass('to-front', false);
            baudrate.toggleClass('to-front', false);
            unitAddressElement.classList.remove('to-front');
            if (unitAddressElement.nextElementSibling) {
                unitAddressElement.nextElementSibling.classList.remove('to-front');
            }
            if (btnStart.nextElementSibling) {
                btnStart.nextElementSibling.classList.remove('to-front');
            }
            summaryAlarmCard.classList.remove('to-front');
            appExit.classList.remove('to-front');
    }
});
