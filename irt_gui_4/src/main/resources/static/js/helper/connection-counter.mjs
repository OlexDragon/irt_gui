
const sessionId = 'sessionId' + Math.random().toString(16).slice(2);

const worker = new Worker(
    new URL('./connection-counter-worker.js', import.meta.url),
    { type: 'module' }
);

let onCount;

worker.onmessage = ({ data }) => {
    if (data.type === 'tick')
        update();
};

let started = false;
export async function start(callback) {

    if (started)
        throw new Error('Connection counter is already started.');

    started = true;
    onCount = callback;
    await update();

    worker.postMessage({
        type: 'start',
        delay: 20000
    });

    window.addEventListener('beforeunload', remove);
}

export function stop() {

    if (!started)
        return;

    started = false;
    onCount = null;

    worker.postMessage({ type: 'stop' });
    window.removeEventListener('beforeunload', remove);
    remove();
}

async function update() {
    try {
        const response = await fetch('/connection/add', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            },
            body: new URLSearchParams({
                connectionId: sessionId
            })
        });

        if (!response.ok)
            throw new Error(`HTTP ${response.status}`);

        const count = await response.json();

        onCount?.(count);

        return count;

    } catch (error) {
        console.error('Error updating connections:', error);
    }
}

function remove() {
    navigator.sendBeacon(
        '/connection/remove',
        new URLSearchParams({
            connectionId: sessionId
        })
    );
}

export async function getOtherConnections() {

    const count = await update();

    return count == null
        ? null
        : Math.max(0, count - 1);
}