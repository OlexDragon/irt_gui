import packetId from '../packet/packet-properties/packet-id.js'
import ModuleLoader from './module-loader.js'

export default class MeasurementLoader{

	#unitType;
	#packetId;
	#controllerLoader;
	#parameterLoader;

	#controller;
	#parameter;;


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
			this.#packetId = packetId.measurementIRPC;
			loadC = this.#controllerLoader.load('./controller/controller-meas-irpc.js');
			loadP = this.#parameterLoader.load('./packet/parameter/irpc.js');
			break;

		case 'CONVERTER':
		case 'CONVERTER_KA':
			this.#packetId = packetId.measurement;
			loadC = this.#controllerLoader.load('./controller/controller-meas-fcm.js');
			loadP = this.#parameterLoader.load('./packet/parameter/measurement-fcm.js');
			break;

		case 'REFERENCE_BOARD':
			this.#packetId = packetId.measurement;
			loadC = this.#controllerLoader.load('./controller/controller-meas-fcm.js');
			loadP = this.#parameterLoader.load('./packet/parameter/measurement-rcm.js');
			break;

		case 'CONTROLLER_ODRC':
			this.#packetId = packetId.measurement;
			loadC = this.#controllerLoader.load('./controller/controller-measurement.js');
			loadP = this.#parameterLoader.load('./packet/parameter/measurement-odrc.js');
			break;

		default:
			console.log('Load by default', unitType);
		case 'BAIS_LOW_POWER':
		case 'BAIS':
			this.#packetId = packetId.measurement;
			loadC = this.#controllerLoader.load('./controller/controller-measurement.js');
			loadP = this.#parameterLoader.load('./packet/parameter/measurement-buc.js');
		}

		loadC.then(this.setController.bind(this));
		loadP.then(this.setParameter.bind(this));
		Promise.all([loadC, loadP])
		.then(()=>callBack(this.#controller));
	}

	get packetId(){
		return this.#packetId;
	}

	get controller(){
		return this.#controller;
	}

	get parameter(){
		return this.#parameter;
	}

	setController(c){
		const {default: Controller} = c;
		this.#controller = Controller;
	}

	setParameter(p){
		this.#parameter = p;
	}
}
