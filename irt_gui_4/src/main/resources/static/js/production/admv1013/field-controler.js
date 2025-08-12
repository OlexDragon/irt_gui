export default class FieldController{

	#$field;
	#mask;
	#_mask;
	#shift;

	#callBac;

	constructor($field, mask, shift){
		this.#$field = $field.change(this.#onChange.bind(this));
		this.#mask = mask;
		this.#_mask = mask>>shift;
		this.#shift = shift;
	}

	get value(){
		return (+this.#$field.val())<<this.#shift;
	}
	set value(val){
		this.#$field.prop('disabled', false);

		const v = (val&this.#mask)>>this.#shift;
		const str = this.#$field.val()
		const value = +str;
		if(str && (this.#$field===document.activeElement || v===value))
			return;

		this.#$field.val(v);
	}

	/**
     * @param {Function} cb
     */
	set change(cb){
		this.#callBac = cb;
	}

	#onChange({currentTarget : el}){
		const value = +el.value;
		const v = value&this.#_mask
		if(value!==v)
			el.value = v;

		if(this.#callBac){
			this.#callBac({mask: ~this.#mask, value: v<<this.#shift});
		}
	}
}