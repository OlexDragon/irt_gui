import {type as unitType} from './panel-info.js'

let ut;
export default function(){

	const t = unitType.slice(0,2).join('.').toString();
	if(ut?.type !== t)
		ut = new UnitType(t);
	return ut;
}

class UnitType{
	_measurement;
	#type;
	constructor(type){
		this.#type = type;
		console.log(this.#type);
	}

	get type(){
		return this.#type;
	}

	get id(){
		return unitTypes[this.#type];
	}
}

const unitTypes = {};
unitTypes[310.21] = 'irpc'	// Intelligent Redundant Protection Controller