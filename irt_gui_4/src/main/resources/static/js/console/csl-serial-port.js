// csl-serial-port.js

import Baudrate from '../classes/baudrate.js'
import { canExit, stop as stopCounter } from '../helper/connection-counter-ui.mjs';


function hideElement(element) {
    element.style.display = 'none';
}

// DOM element references
const serialPortSelect = document.getElementById('serialPort');
serialPortSelect.addEventListener('change', serialPortChange);
serialPortSelect.nextElementSibling.textContent = txtSerialPort232;

const baudrateElement = document.getElementById('baudrate');
export const baudrate = new Baudrate(baudrateElement, { storageKey: 'console' });
export let serialPort;

hideElement(document.getElementById('btnStart')?.parentElement);
hideElement(document.getElementById('unitAddress')?.parentElement);
hideElement(document.getElementById('fwUpgrade')?.parentElement);

const summaryAlarmCard = document.getElementById('summaryAlarmCard');
hideElement(summaryAlarmCard);

const guiLink = document.getElementById('consoleLink');
guiLink.textContent = 'GUI';
guiLink.href = '/';

document.getElementById('appExit').addEventListener('click', async () => {

    try {

        if (await canExit()) {

            stopCounter();
            await showExitModal();

            fetch('/exit', {
                keepalive: true
            });
        }

    } catch (error) {

        console.error(error);

    }

});

// Event listener for serial port change
function serialPortChange() {
    serialPort = serialPortSelect.value;
    localStorage.setItem('cslSerialPort', serialPort);
}



(async function getPortNames() {

    try {
        const response = await fetch('/serial/ports');

        if (!response.ok) {
            try {
                const errorData = await response.json();
                if (errorData?.message) {
                    if (showError)
                        showToast(errorData.error, errorData.message, 'text-bg-danger bg-opacity-50');
                } else {
                    const status = f_alarmStatus('Closed');
                    summaryAlarmTitle.textContent = status.text;
                }
            } catch {
                const status = f_alarmStatus('Closed');
                summaryAlarmTitle.textContent = status.text;
            }
            return;
        }

        const ports = await response.json();

        const savedSerialPort = localStorage.getItem('cslSerialPort');
        ports.forEach(name => {
            const selected = savedSerialPort === name;
            if (selected)
                serialPort = name;
            const option = document.createElement('option');
            option.text = name;
            option.selected = selected;
            serialPortSelect.appendChild(option);
            if (selected) {
                btnStart.disabled = false;
                serialPortSelect.dispatchEvent(new Event('change'));
            }
        });

    } catch (error) {
        console.error(error);
    }
})();

const modal = document.getElementById('modal');
async function showExitModal() {

    let html;

    try {

        const response = await fetch('/modal/exit');

        if (!response.ok)
            throw new Error(`HTTP ${response.status}`);

        html = await response.text();

    } catch (error) {

        console.error('Error loading exit modal:', error);

        html = `
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content">
                    <div class="modal-header">
                        <h5 class="modal-title">${translate('modal.exit', `guiClosed`)}</h5>
                    </div>
                    <div class="modal-body">
					${translate('modal.exit', `guiClosedMessage`)}
                    </div>
                </div>
            </div>
        `;
    }

    modal.innerHTML = html;
    modal.setAttribute('data-bs-backdrop', 'static');

    const bsModal = new bootstrap.Modal(modal);
    bsModal.show();
}
