// controller-value.js
import ValuePanel from '../classes/value-panel.js'
import CheckboxHelper from './helper/checkbox-helper.mjs'

export default class ControllerValue extends ValuePanel {

	#toggleController;

    constructor(body) {
        super(body);

        this._fields.value = body.querySelector('input.value');
        if (!this._fields.value)
            throw new Error(`${this._name}: There cannot be empty fields.`);

        const div = body.querySelector(`div.${this._name}Enable`);
        if (div){
			const toggle = div.querySelector('input');
            this.#toggleController = new CheckboxHelper(toggle, (state)=>this.#stateChange(state));
		}
    }

    set value(value) {

        const raw = this._fields.value.value

        if (raw === '' || Number(raw) != value)
            this._fields.value.value = value;
        super.value = value;
    }

    get value() {
        return this._fields.value.value;
    }

    get name() {
        return this._name;
    }

	get enable(){
		return this._fields.toggle?.checked
	}

	/**
     * @param {any} enable
     */
	set anable(enable){
		if(!this.#toggleController)
			return;
		this.#toggleController.checked = enable
	}

    _escape() {
        return this._fields.value.value;
    }

	#stateChange({ checked }){
		this._sendChange({enable: checked });
	}
}