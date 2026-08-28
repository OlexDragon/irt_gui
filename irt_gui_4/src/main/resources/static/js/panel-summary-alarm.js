// panel-summary-alarm.js

import * as serialPort from './serial-port.js'
import { info as infoType } from './packet/packet-properties/packet-type.js'
import { info as infoId } from './packet/packet-properties/packet-id.mjs'
import { info as paranId, parser } from './packet/parameter/alarm.js'
import { status as f_alarmStatus } from './packet/parameter/value/alarm-status.js'
import { info as infoGroup } from './packet/packet-properties/group-id.mjs'
import UnitAddressScannerUI from './helper/unit-address-scanner/unit-address-scanner-ui.mjs';

const card = document.getElementById('summaryAlarmCard');
const title = document.getElementById('summaryAlarmTitle');
title.addEventListener('click', tripleClick);

const statusChangeEvent = [];

let oldValue;
let interval;

const action = {
    type: infoType.request,
    packetId: infoId.alarmSummary,
    groupId: infoGroup.alarm,
    data: {
        codes: [paranId.summaryStatus]
    },
    function: 'f_SummaryAlarms',
    f_error: onError
};

export function start() {
    oldValue = undefined;
    action.busy = false;
    run();
    clearInterval(interval);
    interval = setInterval(run, 2000);
}

export function stop() {
    clearInterval(interval);
}

export function onStatusChange(e) {
    statusChangeEvent.push(e);
}

export function textToStatus(text) {
    const status = f_alarmStatus(text);
    setStatus(status);
}

export function closed() {
    return oldValue?.severities === 'Closed'
}

function run() {
    if (!serialPort.doRun()) {
        stop();
        return;
    }

    if (action.busy) {
        serialPort.blink(card, 'connection-busy');
//        console.warn('busy');
        return
    }

    action.busy = true;

    serialPort.postObject(card, action);
}

function removeClasses() {
    const classes = title.className
        .split(' ')
        .filter(c => c.startsWith('text-bg-'));
    title.classList.remove(...classes);
}

action.f_SummaryAlarms = function(packet) {
    $modal?.hide();

    packet.payloads?.forEach(pl => {
        const value = parser(pl.parameter.code)(pl.data);
        setStatus(value);
    });
}

function setStatus(value) {
    if (value.severities != oldValue?.severities) {
        serialPort.showError && serialPort.showToast(
            value.severities,
            value.text,
            (value.boorstrapClass ?? 'text-bg-danger') + ' bg-opacity-50'
        );
        statusChangeEvent.forEach(e => e(value));

        if (oldValue)
            title.classList.remove(oldValue.boorstrapClass);
        else
            removeClasses();

        if (value.boorstrapClass) {
            title.classList.add(value.boorstrapClass);
        }

        const severity = value.severities;
        if (title.textContent !== value.severityTranslated) {
            title.textContent = value.severityTranslated;
            title.title = value.text;
        }

        oldValue = value;
        switch (value.severities) {
            case 'SP Error':
            case 'Closed':
                serialPort.stop();
        }
    }
}

function tripleClick({ detail, ctrlKey, shiftKey }) {
    if (detail != 3)
        return;

    if (shiftKey && ctrlKey) {
        unitAddressScan();
        return;
    }

    const page = ctrlKey ? 'upgrade' : 'production';
    const url = new URL(page, window.location.href)
    setTimeout(() => { window.open(url, '_blank'); }, 100);
}

let $modal;
function onError(error) {
    if ($modal)
        return;

    switch (error) {
        case "The port is locked.": {
            const modalDiv = document.createElement('div');
            modalDiv.className = 'modal fade';
            modalDiv.setAttribute('data-bs-backdrop', 'static');
            modalDiv.tabIndex = -1;

            const modalDialog = document.createElement('div');
            modalDialog.className = 'modal-dialog modal-dialog-centered';

            const modalContent = document.createElement('div');
            modalContent.className = 'modal-content';

            // Header
            const modalHeader = document.createElement('div');
            modalHeader.className = 'modal-header';
            const title = document.createElement('h5');
            title.className = 'modal-title';
            title.textContent = 'Serial Port Locked';
            modalHeader.appendChild(title);
            modalContent.appendChild(modalHeader);

            // Body
            const modalBody = document.createElement('div');
            modalBody.className = 'modal-body';
            const p1 = document.createElement('p');
            p1.textContent = 'The serial port is locked. Unlocking it may take several minutes.';
            const p2 = document.createElement('p');
            p2.textContent = 'If it remains locked, please press the "Stop" button to break the connection.';
            modalBody.appendChild(p1);
            modalBody.appendChild(p2);
            modalContent.appendChild(modalBody);

            // Footer
            const modalFooter = document.createElement('div');
            modalFooter.className = 'modal-footer';
            const stopBtn = document.createElement('button');
            stopBtn.type = 'button';
            stopBtn.className = 'btn btn-primary';
            stopBtn.textContent = 'Stop';
            stopBtn.addEventListener('click', () => {
                serialPort.stop();
                bootstrap.Modal.getInstance(modalDiv).hide();
            });
            modalFooter.appendChild(stopBtn);
            modalContent.appendChild(modalFooter);

            modalDialog.appendChild(modalContent);
            modalDiv.appendChild(modalDialog);
            document.body.appendChild(modalDiv);

            $modal = new bootstrap.Modal(modalDiv);
            modalDiv.addEventListener('hidden.bs.modal', () => {
                modalDiv.remove();
                $modal = null;
            });
            $modal.show();
            break;
        }
    }
}

// Unit Address Scanner
const addressScanner = new UnitAddressScannerUI(card);
function unitAddressScan() {
    stop();
    addressScanner.start();
}

