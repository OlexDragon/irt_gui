import ValuePanel from './value-panel.js'

export default class ControllerValue extends ValuePanel{

	constructor(name, $body){
		super(name, $body);

		this._fields.$value = $body.find(`input.value`);
		if(!this._fields.$value.length)
			throw new Error(name + ': There cannot be empty fields.')
	}

	/**
     * @param {number} v
     */
	set value(v){
		if(this._fields.$value.val()!==v)
			this._fields.$value.val(v);
		super.value = v;
	}

	get value(){
		return this._fields.$value.val();
	}

	get name(){
		return this._name;
	}

	_escape(){
		return this._fields.$value.val();
	}
}