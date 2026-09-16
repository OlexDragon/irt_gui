// csl-serial-port.mjs

import Baudrate from '../serial/baudrate.mjs';
import SelectSerialPort from '../serial/select-serial-port.mjs';
import ProgramExit from '../helper/program-exit.mjs';

export default class CslSerialPort {

    #serialPort;
    #baudrate;

    constructor({
        serialPortElement,
        baudrateElement,
        exitElement,
        modal,
        storageKey = 'console',
		callback,
		beforeExit
    }) {

        this.#serialPort = new SelectSerialPort(

            serialPortElement,

            {
                storageKey,
				callback
            }

        );

        this.#baudrate = new Baudrate(

            baudrateElement,

            {
                storageKey
            }

        );

        new ProgramExit(
            exitElement,
            modal,
			{ beforeExit}
        );

    }

    get serialPort() {
        return this.#serialPort;

    }

    get baudrate() {
        return this.#baudrate;

    }

}