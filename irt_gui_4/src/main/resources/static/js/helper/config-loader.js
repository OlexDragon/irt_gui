import packetId from '../packet/packet-properties/packet-id.js'
import ModuleLoader from './module-loader.js'

export default class MeasurementLoader{

	#unitType;
	#packetId;
	#controllerLoader;
	#parameterLoader;

	#controller;
	#parameter;
	#toRead;

	#getAll;

	constructor(unitType){
		this.#controllerLoader = new ModuleLoader();
		this.#parameterLoader = new ModuleLoader();
		if(unitType)
			this.setUnitType(unitType);
	}

	get unitType(){
		return this.#unitType;
	}

	setUnitType(unitType, callBack){

		if(JSON.stringify(this.unitType) === JSON.stringify(unitType)){
			callBack(this.#controller);
			return;
		}

		this.#unitType = unitType;
		let loadC;
		let loadP;

		switch(unitType.name){

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
			console.warn(unitType);
		case 'BAIS':
			this.#packetId = packetId.configAll;
			loadC = this.#controllerLoader.load('./controller/controller-config-buc.js');
			loadP = this.#parameterLoader.load('./packet/parameter/config-buc.mjs');
		}

		loadC.then(this.#setController.bind(this));
		loadP.then(this.#setParameter.bind(this));
		Promise.all([loadC, loadP]).then(()=>callBack(this.#controller));
	}

	get packetId(){
		return this.#packetId;
	}

	get toRead(){
		return this.#toRead;
	}

	get controller(){
		return this.#controller;
	}

	get parameter(){
		return this.#parameter.default;
	}

	#setParameter(p){
		this.#parameter = p;
		this.#getAll();
	}

	#setController(c){
		const {default: Controller} = c;
		this.#controller = Controller;
	}
}
