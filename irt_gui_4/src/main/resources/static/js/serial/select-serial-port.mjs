// select-serial-port.mjs

import SelectHelper from '../controller/helper/select-helper.mjs';

export default class SelectSerialPort extends SelectHelper {

    #storageKey;

    #callback;

    constructor(selectElement, {
        storageKey = 'gui',
        callback
    } = {}) {

        super(selectElement, data => this.#change(data));

        this.#storageKey = `serialPort:${storageKey}`;
        this.#callback = callback;

        this.loadPorts();

    }

    #change(data) {

        localStorage.setItem(this.#storageKey, data.value);
        this.#callback?.(data);
    }

    async loadPorts() {

        try {

            const response = await fetch('/serial/ports');

            if (!response.ok) {
                this.onError?.(await this.safeJson(response));
                return;

            }

            const ports = await response.json();

            if (!ports?.length) {

                this.onPortsEmpty?.();

                return;

            }
            this.fill(
                ports.map(name => ({
                    value: name,
                    name
                })),
				
                localStorage.getItem(this.#storageKey),
				
                {
                    createDefault: true
                }
            );


        } catch (error) {
            console.error(error);
        }

    }

    async safeJson(response) {

        try {
            return await response.json();
        } catch {
            return null;

        }

    }

}