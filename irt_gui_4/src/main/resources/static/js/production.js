// production.js

import * as serialPort from './serial-port.js';
import './production/cal-mode.js';
import './panel-measurement.js';
import './panel-config.js'
import './user-panels.js'
import './panel-units.js'
import { onStatusChange } from './panel-summary-alarm.js';
import { type as unitType, onStartAll, onTypeChange, onSerialChange } from './panel-info.js';
import ProfileButton from './helper/profile-button.mjs';
import { onModules } from './panel-units.js';

const NAV_NAME = 'productionNav';
const productionNav = document.querySelector('.navbar');
const productionContent = document.querySelector('#productionContent');

productionNav
    .querySelectorAll('input[name="productionNav"]')
    .forEach(input => input.addEventListener('change', onNavChange));

let controller;

onTypeChange(typeChange);
onSerialChange(serialNumberChange);
onStartAll(yes => yes ? start() : stop());
onStatusChange(statusChange);

function statusChange(alarmStatus) {
    switch (alarmStatus.severities) {

        case 'Closed':
        case 'TIMEOUT':
        case 'Stopped':
            stop();
            break;

        default:
            start();
    }
}

onModules(modules => {
    console.log('Modules found:', modules);

    if (Object.keys(modules).length > 1)
        showModuleScanButton();
});

function showModuleScanButton() {

};

let interval;
function start() {
    if (interval)
        return;

    productionNav
        .querySelectorAll('input[name="productionNav"]:checked')
        .forEach(({ id }) => loadController(id));
}

function stop() {
    interval = clearInterval(interval);
}

function onNavChange({ currentTarget: { id } }) {
    productionNav
        .querySelectorAll('input[name="productionNav"]')
        .forEach(input => input.disabled = true);

    loadController(id);
}

async function loadController(id) {

    if (controller?.constructor.name !== id)
        switch (id) {

            case 'cbDACs':
                {

                    switch (unitType?.name) {

                        case 'REFERENCE_BOARD':
                            {
                                const { default: c } = await import('./production/controller-rcm.js');
                                setController(id, c);
                                break;
                            }

                        default:
                            const { default: c } = await import('./production/controller-dacs.js');
                            setController(id, c);
                    }
                    break;
                }

            case 'potentiometersId':
                const { default: c } = await import('./production/controller-pots.js');
                setController(id, c);
                break;

            case 'admv1013':
                {
                    const { default: c } = await import('./production/controller-admv1013-converter.js');
                    setController(id, c);
                    break;
                }

            case 'admv1013Bias':
                {
                    const { default: c } = await import('./production/controller-admv1013-bias.js');
                    setController(id, c);
                    break;
                }

            case 'stuw81300':
                {
                    const { default: c } = await import('./production/controller-stuw81300-converter.js');
                    setController(id, c);
                    break;
                }

            case 'stuw81300Bias':
                {
                    const { default: c } = await import('./production/controller-stuw81300-bias.js');
                    setController(id, c);
                    break;
                }

            case 'cbDump':
                {
                    const { default: c } = await import('./production/controller-dump.js');
                    setController(id, c);
                    break;
                }

            default:
                controller = undefined;
                console.warn('Unknown controller:', id);
        }

    if (controller) {
        run();
        clearInterval(interval);
        interval = setInterval(run, 3000);
        productionNav
            .querySelectorAll('input[name=productionNav ]')
            .forEach((el) => el.disabled = false);
        typeChange();
    }
}

function setController(id, c) {
    controller = new c($(productionContent));
    controller.name = id;
    controller.onSet = onSet;
    if (unitType)
        controller.typeName = unitType.name;
}

function run() {

    if (!unitType)
        return;

    const action = controller.action;
    action.f_error = packetError;
    if (action.packetError) {
        console.warn('Packet error: ', action.packetError);
        action.packetError = undefined;
        stop();
        return;
    }
    if (action.doNotSend || action.data.value === undefined) {
        //		console.log(action.doNotSend, action.data);
        return;
    }
    if (action.busy) {
        console.log('busy');
        return;
    }

    action.busy = true;

    serialPort.postObject(productionContent, action);
}

let moduleScanner;

async function scanModules() {
    if (!moduleScanner) {
        moduleScanner = await import('./module-scanner.mjs');
    }

    moduleScanner.scan();
}

// the work is not finished
// This is to determine the serial number of each module.
function addModuleScanButton() {
    const button = document.createElement('button');

    button.type = 'button';
    button.className = 'btn btn-outline-primary';
    button.textContent = 'Scan Modules';

    button.addEventListener('click', scanModules);

    productionNav.append(button);
}

function onSet(action) {
    serialPort.postObject(productionContent, action);
}


let savedUnitType;
function typeChange() {
    if (!unitType ||
        (unitType.type === savedUnitType?.type && unitType.subtype === savedUnitType?.subtype))
        return;

    savedUnitType = unitType;

    profileButton.setType(unitType);
    //	console.warn('Device Type Change:', unitType);
    const admv = productionNav.querySelectorAll('.pllRegisters');
    const dType = unitType?.name;
    if (controller)
        controller.typeName = dType;
    switch (dType) {

        case 'CONVERTER_KA':
        case 'KA_BIAS':
            if (!admv.length) {

                let admvId;
                let stuwId;
                if (dType == 'CONVERTER_KA') {
                    admvId = 'admv1013';
                    stuwId = 'stuw81300';
                } else {
                    admvId = 'admv1013Bias';
                    stuwId = 'stuw81300Bias';
                }

                addNavButton('admv1013', 'ADMV 1013', 'ADMV1013', 'pllRegisters');
                addNavButton('stuw81300', 'STUW 81300', 'STUW81300', 'pllRegisters');
                addNavButton('potentiometersId', 'POTs', 'Potentiometers');
            }
            return;

        case 'REFERENCE_BOARD':
            const el = productionNav
                .querySelector(`input[name="${NAV_NAME}"]:checked`)
            loadController(el.id)
            break;

        default:
            console.log(unitType);
        case 'BAIS':
        case 'CONTROLLER':
        case 'CONTROLLER_IRPC':
        case 'CONTROLLER_ODRC':
    }

    if (admv.length) {
        productionNav.querySelector('label[for=admv1013]').remove();
        admv.remove();
    }
}

function addNavButton(id, text, title, className = '') {
    const div = document.createElement('div');
    div.className = `col-auto ms-1 ${className}`;

    const input = document.createElement('input');
    input.id = id;
    input.name = NAV_NAME;
    input.type = 'radio';
    input.className = 'btn-check';
    input.autocomplete = 'off';

    input.addEventListener('change', onNavChange);

    const label = document.createElement('label');
    label.htmlFor = id;
    label.title = title;
    label.textContent = text;
    label.className = 'btn btn-outline-primary';

    div.append(input, label);
    productionNav.append(div);

    return input;
}

const profileButton = new ProfileButton(productionNav);
let serialNumber;

async function serialNumberChange(sn) {
    console.log('Serial Number Change:', sn);

    if (serialNumber === sn)
        return;

    serialNumber = sn;

    addCalibrationButton(sn);

    await profileButton.setSerialNumber(sn);
}

function addCalibrationButton(sn) {
    productionNav
        .querySelectorAll('.cal-link')
        .forEach(el => el.remove());

    const div = document.createElement('div');
    div.className = 'col-auto cal-link ms-2';

    if (admin) {
        const link = document.createElement('a');
        link.className = 'btn btn-outline-info';
        link.target = '_blank';
        link.href = `http://irt/calibration?sn=${encodeURIComponent(sn)}`;
        link.textContent = 'Calibration';

        div.append(link);
    }

    setTimeout(() => productionNav.append(div), 100);
}


function packetError(dara) {
    if (data.error.packet?.header?.error === 0)
        return;
    console.warn('The Packet has an error. Controller stops.\n', data);
    stop();
}