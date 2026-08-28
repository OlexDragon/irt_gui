import * as serialPort from '../../serial-port.js'

import packetId from '../../packet/packet-properties/packet-id.mjs';
import packetType from '../../packet/packet-properties/packet-type.js';

export default class BucHelper {

    static #nextId = Object.keys(packetId).length;

    #callBack;
    #range;
    #element;
	#delay;

    #action = {
        data: {},
        function: 'f_analyze'
    };

    #interval;

    constructor({ groupId, parameter, range, element, callBack, delay = 5000 }) {
        this.#callBack = callBack;
        this.#range = range;
        this.#element = element;
		this.#delay = delay;

        if (BucHelper.#nextId > 0xFFFF)
            throw new Error('No helper IDs available.');

        const id = BucHelper.#nextId++;
        this.#action.packetId = {
            name: `${parameter.name}Helper`,
            code: id
        };

        this.#action.groupId = groupId;

        this.#action.type = {
            code: packetType.request,
            name: 'request'
        };

        this.#action.data.codes =
            range == null
                ? [parameter]
                : [range, parameter];

        this.#action.f_analyze = value => this.#analyze(value);
        this.#action.f_error = packet => this.#error(packet);
    }

    start() {
        if (!this.#action)
            return;

        this.stop();
        this.#run();
        this.#interval = setInterval(() => this.#run(), this.#delay);
    }

    stop() {
        clearInterval(this.#interval);
        this.#interval = null;
    }

    get isRunning() {
        return !!this.#interval;
    }

    #analyze(value) {
        if (this.#range != null) {
            const codes = this.#action.data.codes;
            const index = codes.indexOf(this.#range.code);

            if (index !== -1) {
                codes.splice(index, 1);
                this.#action.update = true;
            }

            this.#range = null;
        }

        this.#callBack?.(value);
    }

    #run() {
        if (this.#action)
            serialPort.postObject(this.#element, this.#action);
    }

    removeParameterCode(code) {
        const codes = this.#action.data.codes;
        const index = codes.findIndex(p => p.code === code);
        if (index !== -1) {
            codes.splice(index, 1);
			this.#action.update = true;
        }
        if (!codes.length)
            this.destroy();
    }

    #error(data) {
        const header = data?.error?.packet?.header;

        if (header?.error) {
            console.warn('The package returned an error. This helper was destroyed.', this.#action);
            this.destroy();
        } else
            console.log(data);
    }

	#destroyed = false;
    destroy() {
		if(this.#destroyed)
			return;
        this.stop();
        this.#action = null;
		this.#destroyed = true;
    }
}
