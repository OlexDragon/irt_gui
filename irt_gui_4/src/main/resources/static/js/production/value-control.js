
export default class ValueControl{

	#$value
	#$valueToSet
	#$range
	#$step

	#oldValue;

	constructor($value, $valueToSet, $range, $step){
		this.#$value = $value.val($valueToSet.val());
		const onChange = this.#onChange.bind(this);
		this.#$valueToSet = $valueToSet.addClass('input-selected').change(onChange);
		this.#$range = $range.prop('disabled', false).val($valueToSet.val()).change(onChange).on('input', this.#rangeOnInput.bind(this));
		this.#$step = $step;
		this.#oldValue = +$valueToSet.val();
	}

	get value(){
		return +this.#$valueToSet.val();
	}
	set value(value){
		if((+this.#$value.val())!=value)
			this.#$value.val(value);
		if(!this.#$valueToSet.is(':focus') && this.#$range[0] !== document.activeElement && (+this.#$valueToSet.val()) !== value){
			this.#$valueToSet.val(value);
			this.#$range.val(value);
			this.#oldValue = value;
		}
	}

	is($el){
		return this.#$valueToSet.is($el);
	}

	focusOut(){
		this.#$valueToSet.removeClass('input-selected').off('change');
		this.#$range.off();
	}

	#callBack;
	onChange(cb){
		this.#callBack = cb;
	}

		#rangeInputCout = 0;
	#onChange({currentTarget : el}){
		switch(el.type){

		case 'number':
		{
			const fVal = el.value;
			let rVal = this.#$range.val();
			this.#$range.val(fVal);
			rVal = this.#$range.val();
			if(rVal !== fVal)
				this.#$valueToSet.val(rVal);
			rVal = +rVal;

			if(this.#oldValue !== rVal){
				this.#oldValue = rVal;
				this.#callBack(el);
			}

			break;
		}

		case 'range':
		{
			const step = this.#$step.val();
			const fVal = +this.#oldValue;
			let rVal = +el.value;
			if(step && this.#rangeInputCout===1){
				if(fVal<rVal)
					rVal = fVal + parseInt(step);
				else
					rVal = fVal - step;
				this.#$range.val(rVal);
				rVal = this.#$range.val();
			}
			this.#$valueToSet.val(rVal);
			rVal = +rVal;

			if(this.#oldValue !== rVal){
				this.#oldValue = rVal;
				const {dataset} = this.#$valueToSet.get(0);
				this.#callBack({dataset: dataset, value: rVal});
			}
			break;
		}

		default:
			console.warn(el.type);
		}
		this.#rangeInputCout=0;
	}

	#rangeOnInput({currentTarget:{value}}){
		++this.#rangeInputCout;
		this.#$valueToSet.val(value);
	}
}