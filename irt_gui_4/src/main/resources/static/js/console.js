import CslSerialPort from './serial/csl-serial-port.mjs';

const cslSerialPort = new CslSerialPort({

    serialPortElement:
        document.getElementById('serialPort'),

    baudrateElement:
        document.getElementById('baudrate'),

    exitElement:
        document.getElementById('appExit'),

    modal:
        document.getElementById('modal')

});

const clearTextBtn = document.getElementById('clearText');
clearTextBtn.addEventListener('click', clearText);

const commandInput = document.getElementById('commandInput');

commandInput.addEventListener('change', sendCommand);
commandInput.addEventListener('keydown', function(event) {
    if (event.key === 'Enter') {
        event.preventDefault();
        sendCommand();
    }
});

const sendCommandBtn = document.getElementById('sendCommandBtn');
sendCommandBtn.addEventListener('click', sendCommand);

const taConsole = document.getElementById('taConsole');
taConsole.addEventListener('select', onSelect);

const commandHistory = document.getElementById('commandHistory');
commandHistory.addEventListener('select', onSelect);

const history = new Set();

let path;
function sendCommand() {
	const serialPort = cslSerialPort.serialPort.value;
    if (!serialPort) {
        alert('No serial port selected');
        return;
    }

    sendCommandBtn.disabled = true;

    const br = cslSerialPort.baudrate.baudrate;
    const command = commandInput ? commandInput.value.trim() : '';
    history.add(command);
    const str = Array.from(history).join("\n");

    commandHistory.value = str;
    resizeHistory();

    // Replace $.get with fetch
    fetch(`/console/rest/send?sp=${serialPort}&br=${br}&command=${encodeURIComponent(command)}`)
        .then(response => {
            if (!response.ok) {
                throw response;
            }
            return response.json();
        })
        .then((data) => {

            const answer = String.fromCharCode.apply(null, data.answer);
            if (!answer) {
                path = '>';
            }

            if (taConsole) {
                taConsole.value = taConsole.value + answer;
                taConsole.scrollTop = taConsole.scrollHeight;
            }

            const split = answer.split('\n');
            if (split.length) {
                path = split[split.length - 1];
            } else {
                path = answer;
            }
        })
        .catch((err) => {
            console.error(err);

            if (err instanceof Response) {
                err.text().then(text => alert(text));
            } else {
                alert(err.message || 'An error occurred');
            }
        })
		.finally(()=>{
			sendCommandBtn.disabled = false;
		});
}

function onSelect({ currentTarget: el }) {
    console.log(el.selectionStart, el.selectionEnd);
    if (commandInput) {
        commandInput.value = el.value.substring(el.selectionStart, el.selectionEnd).trim();
    }
}

function clearText() {
    if (taConsole) {
        taConsole.value = '';
    }
}

function resizeHistory() {
    if (commandHistory) {
        const ta = commandHistory;
        ta.style.height = "auto"; // Reset height
        ta.style.height = ta.scrollHeight + "px"; // Set new height
    }
}

// Optional: Initial call to resize history if it has content
if (commandHistory && commandHistory.value) {
    resizeHistory();
}