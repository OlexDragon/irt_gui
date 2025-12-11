
export default class ValuePanel{

	_name;
	_value;
	_fields = {};
	
	count = 0;
	multiplier = 1;
	#inFocus = false;

	#onChangeEvents = [];

	constructor(name, $body){
		this._name = name;

//		setTimeout(()=>new bootstrap.Tooltip($rangeInput), 10000);
		this._fields.$range = $body.find(`input.form-range`).on('input', this.#rangeOnInput).change(this.#rangeOnChange);
		this._fields.$input = $body.find(`input.control`).change(this.#inputOnChange).keydown(this.#inputOnKeydown).focus(this.#onFocus).blur(this.#onBlur).on('input', this.#onInpot.bind(this));
		this._fields.$step = $body.find(`input.step`).change(this.#stepOnChange).attr('data-input-name', `${name}-step`);
		if(!(this._fields.$range.length && this._fields.$input.length && this._fields.$step.length))
			throw new Error(name + ': There cannot be empty fields.')

		const $rangeInput = this._fields.$range;
		const $setInput = this._fields.$input;
		let min = $rangeInput.prop('min');
		if(min){
			const m = parseInt(min);
			this.multiplier = m<0 ?-1 : 1
		}else{
			min = 0;
			$rangeInput.attr('min', min);
		}

		const max = $rangeInput.prop('max');
		if(!max){
			$rangeInput.attr('max', 100);
		}

		const step = $rangeInput.prop('step');
		if(!step)
			$rangeInput.attr('step', 1);

		const val = $setInput.val();
		if(!val){
			$setInput.val(min);
			$rangeInput.val(min);
		}else{
			const value = parseFloat(val)
			if(value<min){
				$setInput.val(min);
				$rangeInput.val(min);
			}else if(value>max){
				$setInput.val(max);
				$rangeInput.val(max);
			}else
				$rangeInput.val(value);
		}

		setTimeout(this.#valueFromCookies.bind(this), 100);
	}

	/**
	* @param {callback} onChange
	*/
	set change(onChange){
		this.#onChangeEvents.push(onChange);
	}

	get value(){
		return +this._fields.$input.val();
	}
	set value(newValue){
		this._value = newValue;

		if(this.count || this.#inFocus || newValue===parseFloat(this._fields.$input.val()))
			return;

		this._fields.$input.val(newValue);
		this._fields.$range.val(newValue*this.multiplier);
	}

	get min(){
		return +this._fields.$range.prop('min');
	}
	set min(newMin){

		if (typeof newMin === 'string')
			newMin = parseInt(newMin);

		const minRange = parseInt(this._fields.$range.attr('min'));
		if (newMin === minRange)
			return minRange;

		this._fields.$range.attr('min', newMin);
		this.multiplier = newMin < 0 ? -1 : 1
		const max = parseInt(this._fields.$range.prop('max'));
		if (max <= newMin)
			this._fields.$range.attr('max', newMin + 100);

		return newMin;
	}

	get max(){
		return +this._fields.$range.prop('max');
	}
	set max(newMax){

		if (typeof newMax === 'string')
			newMax = parseInt(newMax);

		const maxRange = parseInt(this._fields.$range.attr('max'));
		if (newMax === maxRange)
			return newMax;

		this._fields.$range.attr('max', newMax);
		return newMax;
	}

	active =()=>{
		$('.tooltip').remove();
		const value = parseFloat(this._fields.$range.val());
		this._fields.$range.filter(':visible').tooltip('dispose').tooltip({title: Math.abs(value)}).tooltip('show');
	};

	tickMarks(set){
		if(!set?.size)
			return;

		const listName = this._name + '-ticks';
		let $tickList;
		if(this._fields.$range.prop('list'))
			$tickList = $('#' + listName).empty();
		else{
			$tickList = $('<datalist>', {id: listName}).insertAfter(this._fields.$range);
			this._fields.$range.attr('list', listName)
		}

		const array = Array.from(set);
		const options = array.map(v=>$('<option>', {value: v}));
		$tickList.append(options);
		this._fields.$range.attr("step", array[1] - array[0]);
	}

	step(newStep){
		if(newStep){
			if(typeof newStep == 'string')
				newStep = parseFloat(newStep);

			const stepRange = parseFloat(this._fields.$range.attr('step'));
			if(newStep==stepRange)
				return stepRange;

			this._fields.$range.attr('step', newStep);
			const stepStr = newStep.toString();
			this.doToFixed = stepStr.includes('.') ? stepStr.split('.')[1].length : undefined;
		}

		return parseFloat(this._fields.$range.attr('step'));
	}
	userStep(){
		return parseFloat(this._fields.$step.val())
	}

	disable(){
		this._fields.forEach($el=>$el.prop('disabled', true));
	}

	#rangeOnInput = (e)=>{
		$('.tooltip-inner').text( e.currentTarget.value * this.multiplier);
		this.count++;
	}

	#rangeOnChange = (e)=>{

		let value;
		const stepStr = this._fields.$step.val();
		const rangeStr = e.currentTarget.value;

		if(stepStr && this.count==1){

			const step = parseFloat(stepStr);
			value = parseFloat(this._fields.$input.val())*this.multiplier; 
			const range = parseFloat(rangeStr);

			if(range<value)
				value = value - step;
			else
				value = value + step;

			this._fields.$range.val(value);
			value = this._fields.$range.val();
		}else
			value = e.currentTarget.value

		const inputValue = Math.abs(value);
		this._fields.$input.val(inputValue);
		this._fields.$range.filter(':visible').tooltip('dispose').tooltip({title: inputValue}).tooltip('show');
		this.#sendChange(inputValue);
		this.count = 0;
	}

	#inputOnChange = ({currentTarget:el})=>{
		const rVal = Math.abs(this._fields.$range.val());
		let value = +el.value;
		if(value!==rVal){

			const step = this._fields.$step.val() || this._fields.$range.prop('step');
			if(step){
				if((rVal-value)<0)
					value = rVal + (+step);
				else
					value = rVal - step;
				el.value = value;
				
			}
		}
		this._fields.$range.val(value*this.multiplier);
		const v = Math.abs(this._fields.$range.val());
		this._fields.$range.filter(':visible').tooltip('dispose').tooltip({title: v}).tooltip('show');

		if(this._value!==v)
			this.#sendChange(v);

		if(rVal===v)
			el.value = v;
	}

	#stepOnChange = (e)=>{
		const {currentTarget:{id, value}} = e;
		if(value){
			const pf = parseFloat(value);
			const step = parseFloat(this._fields.$range.prop('step'));
			if(pf<step){
				e.currentTarget.value = step;
				Cookies.set(`${this._name}-${id}`, step, {expires: 365, path: '/'});
			}
		}
	}

	#inputOnKeydown = (e)=>{

		switch(e.code){

		case 'ArrowDown':
			{
				const step = this._fields.$step.val();
				if(step){
					e.preventDefault();
					const value = e.currentTarget.value - parseFloat(step);
					this._fields.$range.val(value*this.multiplier);
					e.currentTarget.value = Math.abs(this._fields.$range.val());
					this._fields.$range.filter(':visible').tooltip('dispose').tooltip({title: e.currentTarget.value}).tooltip('show').change();
				}
			}
			break;

		case 'ArrowUp':
			{
				const step = this._fields.$step.val();
				if(step){
					e.preventDefault();
					const value = parseFloat(e.currentTarget.value) + parseFloat(step);
					this._fields.$range.val(value*this.multiplier);
					e.currentTarget.value = Math.abs(this._fields.$range.val());
					this._fields.$range.filter(':visible').tooltip('dispose').tooltip({title: e.currentTarget.value}).tooltip('show').change();
				}
			}
			break;

		case 'Escape':
			const escape = this._escape();
			if(escape){
				e.currentTarget.value = escape;
				this.#onInpot(e);
			}
			break;

		default:
			console.log(e.code)
		}
	}
	#onInpot({currentTarget:{value}}){
		this._fields.$range.val(value*this.multiplier);
	}
	#onFocus = ()=>this.#inFocus = true;
	#onBlur = ()=>this.#inFocus = false;
	#valueFromCookies(){
		Object.values(this._fields).forEach($el=>{
			const name = $el.data('inputName');
			const c =Cookies.get(name);
			if(c) $el.val(c);
			if(name) $el.change(this.#valueToCookies).change();
		});
	}
	#valueToCookies(e){
		const {currentTarget:{value, dataset:{inputName}}} = e;
		Cookies.set(inputName, value, {expires: 365, path: '/'});
	}

	#sendChange(toSend){
		this.#onChangeEvents.forEach(e=>e({[this._name]: toSend}));
	}
}
