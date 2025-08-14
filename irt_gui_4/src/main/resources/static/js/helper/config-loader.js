import packetId from '../packet/packet-properties/packet-id.js'
import ModuleLoader from './module-loader.js'

export default class MeasurementLoader{

	#unitType;
	#packetId;
	#controllerLoader;
	#parameterLoader;

	#controller;
	#parameter;;
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

		if(this.unitType === unitType){
			callBack(this.#controller);
			return;
		}

		this.#unitType = unitType;
		let loadC;
		let loadP;

		switch(unitType){
		case 'CONTROLLER_IRPC':
			this.#getAll = this.#forIRPC;
			this.#packetId = packetId.irpc;
			loadC = this.#controllerLoader.load('./controller/controller-irpc.js');
			loadP = this.#parameterLoader.load('./packet/parameter/irpc.js');
			break;

		case 'CONVERTER':
		case 'CONVERTER_KA':
			this.#getAll = this.#forCONVERTER;
			this.#packetId = packetId.configAll;
			loadC = this.#controllerLoader.load('./controller/controller-config-fcm.js');
			loadP = this.#parameterLoader.load('./packet/parameter/config-fcm.js');
			break;

		case 'CONTROLLER_ODRC':
		case 'REFERENCE_BOARD':
			return;

			default:
			console.warn(unitType);
		case 'BAIS':
			this.#getAll = this.#forBUC;
			this.#packetId = packetId.configAll;
			loadC = this.#controllerLoader.load('./controller/controller-config-buc.js');
			loadP = this.#parameterLoader.load('./packet/parameter/config-buc.js');
		}

		loadC.then(this.#setController.bind(this));
		loadP.then(this.#setParameter.bind(this));
		Promise.all([loadC, loadP])
		.then(()=>callBack(this.#controller));
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
		return this.#parameter;
	}

	#setController(c){
		const {default: Controller} = c;
		this.#controller = Controller;
	}

	#setParameter(p){
		this.#parameter = p;
		this.#getAll();
	}

	#forIRPC(){
		this.#toRead = undefined;
	}

	#forCONVERTER() {
		const {gainRange, attenuationRange, frequencyRange, Gain, Attenuation, Frequency, Mute} = this.#parameter.default;
		this.#toRead = {gainRange, attenuationRange, frequencyRange, Gain, Attenuation, Frequency, Mute};
	}

	#forBUC() {
		const {gainRange, attenuationRange, frequencyRange, Gain, Attenuation, Frequency, loSet, LO, Mute} = this.#parameter.default;
		this.#toRead = {gainRange, attenuationRange, frequencyRange, LO, Gain, Attenuation, Frequency, loSet, Mute};
	}
}
