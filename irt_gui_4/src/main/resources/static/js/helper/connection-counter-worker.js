let interval;

self.onmessage = ({ data }) => {
    switch (data.type) {
        case 'start':
            clearInterval(interval);
            interval = setInterval(() => {
                self.postMessage({ type: 'tick' });
            }, data.delay);
            break;

        case 'stop':
            clearInterval(interval);
            interval = undefined;
            break;
    }
};