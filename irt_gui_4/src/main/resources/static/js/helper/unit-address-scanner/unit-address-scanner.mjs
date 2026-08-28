// unit-address-scanner.mjs

import * as serialPort from '../../serial-port.js'
import { info as infoType } from '../../packet/packet-properties/packet-type.js'
import { info as infoId } from '../../packet/packet-properties/packet-id.mjs'
import { info as infoGroup } from '../../packet/packet-properties/group-id.mjs'
import { info as alarmId } from '../../packet/parameter/alarm.js'

const DEFAULT_START = 1
const DEFAULT_END = 255

export default class UnitAddressScanner {
    static #running = false

    #card;
    #address;
    #end;
    #onSent;
    #onFound;
    #onFinish;

    #action = {
        type: infoType.request,
        packetId: infoId.scanIp,
        groupId: infoGroup.alarm,
        data: {
            codes: [alarmId.summaryStatus]
        },
        function: 'f_scanResponse',
        f_scanResponse: packet => this.#response(packet),
        f_error: data => this.#error(data),
        timeout: 80
    }

    constructor(card, { onSent, onFound, onFinish } = {}) {
        this.#card = card;
        this.#onSent = onSent;
        this.#onFound = onFound;
        this.#onFinish = onFinish;
    }

    start(start = DEFAULT_START, end = DEFAULT_END) {
        if (UnitAddressScanner.#running)
            throw new Error('Only one scan can be performed at a time.');

        UnitAddressScanner.#running = true;
        this.#address = start;
        this.#end = end;

        this.#scan();
    }

    stop() {
        if (!UnitAddressScanner.#running)
            return

        UnitAddressScanner.#running = false
        this.#onFinish?.()
    }

    #scan() {
        if (!UnitAddressScanner.#running)
            return

        if (this.#address > this.#end) {
            this.#finish()
            return
        }

        const address = this.#address++

        this.#onSent?.(address)

        const action = {
            ...this.#action,
            unitAddr: address,
            update: true
        }

        serialPort.postObject(this.#card, action)
    }

    #response(packet) {
        if (!UnitAddressScanner.#running)
            return

        const address = packet.linkHeader?.unitAddr[0]

        if (address !== undefined)
            this.#onFound?.(address)

        this.#scan()
    }

    #error(data) {
        if (!UnitAddressScanner.#running)
            return

        if (data?.error?.message !== 'No answer.')
            console.warn(data)

        this.#scan()
    }

    #finish() {
        UnitAddressScanner.#running = false
        this.#onFinish?.()
    }
}