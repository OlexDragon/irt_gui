// config-loader.js
import packetId from '../packet/packet-properties/packet-id.mjs'
import ModuleLoader from './module-loader.js'

export default class ConfigLoader {

    #unitType;
    #packetId;
    #controllerLoader;
    #parameterLoader;

    #controller;
    #parameter;

    constructor(unitType) {
        this.#controllerLoader = new ModuleLoader();
        this.#parameterLoader = new ModuleLoader();
        if (unitType)
            this.setUnitType(unitType);
    }

    get unitType() {
        return this.#unitType;
    }

    setUnitType(unitType, callBack) {

        if (this.#unitType?.name === unitType?.name && this.#unitType?.type === unitType?.type && this.#unitType?.subtype === unitType?.subtype) {
            callBack?.(this.#controller);
            return;
        }

        this.#unitType = unitType;
        let loadC = null;
        let loadP = null;

        switch (unitType.name) {

            case 'LNB':
                this.#packetId = packetId.irpc;
                loadC = this.#controllerLoader.load('./controller/controller-lnb.js');
                loadP = this.#parameterLoader.load('./packet/parameter/lnb.mjs');
                break;

            case 'CONTROLLER_IRPC':
                this.#packetId = packetId.irpc;
                loadC = this.#controllerLoader.load('./controller/controller-irpc.js');
                loadP = this.#parameterLoader.load('./packet/parameter/irpc.mjs');
                break;

            case 'CONTROLLER_ODRC':
                this.#packetId = packetId.odrc;
                loadC = this.#controllerLoader.load('./controller/controller-odrc.js');
                loadP = this.#parameterLoader.load('./packet/parameter/dlrc.mjs');
                break;

            case 'CONVERTER':
            case 'CONVERTER_KA':
                this.#packetId = packetId.configAll;
                loadC = this.#controllerLoader.load('./controller/controller-config-fcm.js');
                loadP = this.#parameterLoader.load('./packet/parameter/config-fcm.mjs');
                break;

            case 'REFERENCE_BOARD':
                this.#packetId = packetId.configAll;
                loadC = this.#controllerLoader.load('./controller/controller-config-rcm.js');
                loadP = this.#parameterLoader.load('./packet/parameter/config-rcm.mjs');
                break;

            default:
                console.warn('[Unknown Unit Type]', unitType); // There is no need to stop the API, this is for informational purposes only. 'warn' command to find out who is calling the setUnitType.
            case 'BAIS':
            case 'CONTROLLER':
                this.#packetId = packetId.configAll;
                loadC = this.#controllerLoader.load('./controller/controller-config-buc.js');
                loadP = this.#parameterLoader.load('./packet/parameter/config-buc.mjs');
        }


        Promise.all([loadC, loadP])
            .then(([controller, parameter]) => {
                this.#setController(controller);
                this.#setParameter(parameter);
                callBack?.(this.#controller);
            })
            .catch(error => {
                console.error('Failed to load configuration modules.', error);
            });

    }

    get packetId() {
        return this.#packetId;
    }

    get controller() {
        return this.#controller;
    }

    get parameter() {
        return this.#parameter;
    }

    #setParameter({ default: parameter }) {
        this.#parameter = parameter;
    }

    #setController({ default: Controller }) {
        this.#controller = Controller;
    }
}
