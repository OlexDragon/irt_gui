// controller-dump-adc.js
import * as serialPort from '../serial-port.js'
import packetId from '../packet/packet-properties/packet-id.mjs'
import groupId from '../packet/packet-properties/group-id.mjs'
import deviceDebug from '../packet/parameter/device-debug.js'
import { parseToString } from '../packet/service/converter.js';

export default class ControllerDumpAdc {

    static DEBUG_ADC_DUMP = 11;
    #running = false;
    #action = {
        packetId: packetId.dumpADC,
        groupId: groupId.deviceDebug,
        data: {
            parameterCode: deviceDebug.debugDump.code,
            value: ControllerDumpAdc.DEBUG_ADC_DUMP
        },
        function: 'f_dumpADC',
        f_dumpADC: (packet) => this.#reaction(packet),
        f_error: (packet) => this.#packetError(packet)
    }

    #card = null
    #rcmInputValue = null
    sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

    constructor(card) {
		if(!card)
			throw new Error('The "card" parameter must be present.');
        this.#card = card;
    }

	get rcmInputValue() {
	    if (!this.#rcmInputValue) 
	        this.#rcmInputValue = this.#card?.querySelector('#rcmInputValue');
	    return this.#rcmInputValue;
	}
    async start() {

        if (this.#running) {
            console.log('Dump ADC is already running.');
            return;
        }

        this.#running = true;
        while (this.#running) {
			const rcmInputValue = this.rcmInputValue
            if (rcmInputValue)
                serialPort.postObject(this.#card, this.#action);
            await this.sleep(3000);
        }
    }

    stop() {
        this.#running = false;
    }

    #reaction(packet) {
        packet.payloads.forEach(pl => {
            const adc = ControllerDumpAdc.parseAdcReport(pl.data);
            const value = Number(this.#rcmInputValue.value);
			const v = adc?.channels?.[0].raw

            if (v && value !== v)
                this.#rcmInputValue.value = v;
        });
    }

    #packetError(packet) {
        console.error('Dump ADC packet error:', packet);
    }

    static ADC_REGEX = /^ADC:\s*([\d.]+)/;
    static CHANNEL_REGEX = /^Ch\s+(\d+):\s*(\d+)\s*\(([\d.]+)\s*V\)$/;
    static parseAdcReport(bytes) {

        if (!bytes?.length)
            return { adc: null, channels: [] };

        try {

            const text = parseToString(bytes);

            const lines = text
                .split(/\r?\n/)
                .map(line => line.trim())
                .filter(Boolean);

            const adcLine = lines.find(line => line.startsWith("ADC:"));

            const adc = adcLine
                ? Number(adcLine.match(ControllerDumpAdc.ADC_REGEX)?.[1])
                : null;

            const channels = lines
                .map(line => {
                    const m = line.match(ControllerDumpAdc.CHANNEL_REGEX);

                    if (!m) return null;

                    return {
                        id: Number(m[1]),
                        raw: Number(m[2]),
                        voltage: Number(m[3])
                    };
                })
                .filter(Boolean);

            return { adc, channels };
        } catch (error) {
            console.error("ADC parse error:", error);
            return {
                adc: null,
                channels: [],
                error: error.message
            };
        }
    }

	destroy() {
	        // Stop the running loop
	        this.#running = false;
	        
	        // Clear DOM references
	        this.#rcmInputValue = null;
	        this.#card = null;
	        
	        // Clear any pending timeouts (from sleep)
	        // Note: sleep promises will resolve but the loop condition will fail
	        
	        console.log('ControllerDumpAdc destroyed');
	    }}