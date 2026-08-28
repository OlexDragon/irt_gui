// controller-config-buc.js

import ControllerConfig from './controller-config.js';
import { controlBuc } from '../packet/parameter/config-buc.mjs';
import BucHelper from './helper/buc-helper.mjs';

const parameters = controlBuc.parameters;

export default class ControllerConfigBuc extends ControllerConfig {

    #helpers = [];

    _onLoad() {
        this.#createRefSourceHelper();
    }

    start() {
        super.start();
        this.#helpers.forEach(h => h.start());
    }

    stop() {
        super.stop();
        this.#helpers.forEach(h => h?.stop());
    }

    destroy() {
        this.#helpers.forEach(h => h?.destroy());
        this.#helpers.length = 0;
        super.destroy();
    }

    #createRefSourceHelper() {
        const element = this._card.querySelector('#refSource').parentElement;
        const helper = new BucHelper({
            groupId: this.groupId,
            parameter: parameters.refSource,
            range: parameters.refCapability,
            callBack: packet => this.#updateRefSource({ packet, helper }),
            element,
            delay: 10000
        });
        this.#helpers.push(helper);
        helper.start();

    }

    #updateRefSource({ packet, helper }) {
        const select = this.controllers.refSource;

        // To get the 'Capability' parameter to appear first, it had to be sorted.
        packet.payloads.sort((a, b) => {
            return b.parameter.code - a.parameter.code
        }).forEach(pl => {
            const parameterCode = pl.parameter.code
            const parser = controlBuc.parser(parameterCode);
            if (!parser) {
                console.error('The required parser was not found.', pl)
                helper.removeParameterCode(parameterCode);
                return;
            }
            const value = parser(pl.data);

            switch (parameterCode) {

                case parameters.refSource.code:
                    select.value = value;
                    break;

                case parameters.refCapability.code:

                    if (value.length < 2)
                        helper.destroy();
                    else
                        helper.removeParameterCode(parameterCode);

                    select.show()
                    select.disabled = value.length === 1;
                    select.fill(value);
            }
        })
    }
}
