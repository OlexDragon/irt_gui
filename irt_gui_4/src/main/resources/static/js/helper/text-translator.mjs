class DeviceTextTranslator {

    #messages = new Map();

    constructor() {
    }

    async load() {
        const response = await fetch('/file/messages');

        if (!response.ok) {
            throw new Error('Unable to load messages');
        }

        const text = await response.text();

        this.#parse(text);
    }

	#parse(text) {

	    this.#messages.clear();

	    for (const line of text.split(/\r?\n/)) {

	        const value = line.trim();

	        if (!value || value.startsWith('#')) {
	            continue;
	        }

	        const separator = value.indexOf('=');

	        if (separator === -1) {
	            continue;
	        }

	        const key = this.#unescape(
	            value.slice(0, separator).trim()
	        );

	        const message = this.#unescape(
	            value.slice(separator + 1).trim()
	        );

	        this.#messages.set(key, message);
	    }
	}

	#unescape(value) {

	    return value
	        .replace(/\\u([0-9a-fA-F]{4})/g, (_, hex) =>
	            String.fromCharCode(parseInt(hex, 16))
	        )
	        .replace(/\\ /g, ' ')
	        .replace(/\\=/g, '=')
	        .replace(/\\:/g, ':')
	        .replace(/\\\\/g, '\\');
	}

	translate(key) {
	    const value = this.#unescape(key);
	    return this.#messages.get(value) ?? value;
	}
}

const translator = new DeviceTextTranslator();
await translator.load();

export default translator;