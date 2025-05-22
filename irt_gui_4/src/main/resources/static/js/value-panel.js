
export default class ValuePanel{

	count = 0;
	multiplier = 1;
	#onChange = [];
	#elements = {};

	constructor($setInput, $rangeInput, $stepInput){

		new bootstrap.Tooltip($rangeInput);
		this.#elements.$range = $rangeInput.on('input', this.#rangeOnInput).change(this.#rangeOnChange);
		this.#elements.$input = $setInput.change(this.#inputOnChange).keydown(this.#inputOnKeyup).focus(this.#onFocus).blur(this.#onBlur);
		this.#elements.$step = $stepInput.change(this.#stepOnChange);

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
		if(step){
			step = parseInt(stepStr);
			doToFixed = stepStr.includes('.') ? stepStr.split('.')[1].length : undefined;
		}else{
			$rangeInput.attr('step', 1);
		}
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
	}
	change(onChange){
		if(typeof onChange == 'number' || typeof onChange == 'string'){
				this.#onChange.forEach(e=>e(onChange));
			return;
		}
				
		this.#onChange.push(onChange);
	}
	value(newValue){

		if(this.count || this.inFocus)
			return;

		if(newValue===undefined){
			return this.#elements.$input.val();

		}else{

			if(newValue==parseFloat(this.#elements.$input.val()))
				return newValue;

			this.#elements.$input.val(newValue).change()
			return this.#elements.$input.val();
		}
	}
	min(newMin){

		if(newMin!=undefined){

			if(typeof newMin === 'string')
				newMin = parseInt(newMin);
			
			const minRange = parseInt(this.#elements.$range.attr('min'));
			if(newMin===minRange)
				return minRange;

			this.#elements.$range.attr('min', newMin);
			this.multiplier = newMin<0 ? -1 : 1
			const max = parseInt(this.#elements.$range.prop('max'));
			if(max<=newMin)
				this.#elements.$range.attr('max', newMin + 100);

			return newMin;
		}
		return parseInt(this.#elements.$range.prop('min'));
	}
	max(newMax){
		if(newMax!=undefined){

			if(typeof newMax === 'string')
				newMax = parseInt(newMax);

			const maxRange = parseInt(this.#elements.$range.attr('max'));
			if(newMax===maxRange)
				return newMax;

			this.#elements.$range.attr('max', newMax);
			return newMax;
		}
		return parseInt(this.#elements.$range.prop('max'));
	}
	active =()=>{
		$('.tooltip').remove();
		const value = parseFloat(this.#elements.$range.val());
		this.#elements.$range.filter(':visible').tooltip('dispose').tooltip({title: value*this.multiplier}).tooltip('show');
	};
	step(newStep){
		if(newStep){
			if(typeof newStep == 'string')
				newStep = parseFloat(newStep);

			const stepRange = parseFloat(this.#elements.$range.attr('step'));
			if(newStep==stepRange)
				return stepRange;

			this.#elements.$range.attr('step', newStep);
			const stepStr = newStep.toString();
			this.doToFixed = stepStr.includes('.') ? stepStr.split('.')[1].length : undefined;
		}

		return parseFloat(this.#elements.$range.attr('step'));
	}
	userStep(){
		return parseFloat(this.#elements.$step.val())
	}
	#rangeOnInput = (e)=>{
		$('.tooltip-inner').text( e.currentTarget.value * this.multiplier);
		this.count++;
	}
	#rangeOnChange = (e)=>{

		let value;
		const stepStr = this.#elements.$step.val();
		const rangeStr = e.currentTarget.value;

		if(stepStr && this.count==1){

			const step = parseFloat(stepStr);
			value = parseFloat(this.#elements.$input.val())*this.multiplier; 
			const range = parseFloat(rangeStr);

			if(range<value)
				value = value - step;
			else
				value = value + step;

			this.#elements.$range.val(value);
			value = this.#elements.$range.val();
		}else
			value = e.currentTarget.value

		const inputValue = value*this.multiplier; 
		this.#elements.$input.val(inputValue);
		this.#elements.$range.filter(':visible').tooltip('dispose').tooltip({title: inputValue}).tooltip('show');
		this.change(inputValue);
		this.count = 0;
	}
	#inputOnChange = (e)=>{
		if(e.currentTarget.value===this.#elements.$range.val())
			return;

		this.#elements.$range.val(e.currentTarget.value*this.multiplier);
		const value = this.#elements.$range.val()*this.multiplier;
		this.#elements.$range.filter(':visible').tooltip('dispose').tooltip({title: value}).tooltip('show');
		if(e.currentTarget.value!==value)
			e.currentTarget.value = value;
	}
	#stepOnChange = (e)=>{
		if(e.currentTarget.value){
			const pf = parseFloat(e.currentTarget.value);
			const step = parseFloat(this.#elements.$range.prop('step'));
			if(pf<step)
				e.currentTarget.value = step;
		}
	}
	#inputOnKeyup = (e)=>{

		switch(e.code){

		case 'ArrowDown':
			{
				const step = this.#elements.$step.val();
				if(step){
					e.preventDefault();
					const value = e.currentTarget.value - parseFloat(step);
					this.#elements.$range.val(value*this.multiplier);
					e.currentTarget.value = this.#elements.$range.val()*this.multiplier;
					this.#elements.$range.filter(':visible').tooltip('dispose').tooltip({title: e.currentTarget.value}).tooltip('show');
				}
			}
			break;

		case 'ArrowUp':
			{
				const step = this.#elements.$step.val();
				if(step){
					e.preventDefault();
					const value = parseFloat(e.currentTarget.value) + parseFloat(step);
					this.#elements.$range.val(value*this.multiplier);
					e.currentTarget.value = this.#elements.$range.val()*this.multiplier;
					this.#elements.$range.filter(':visible').tooltip('dispose').tooltip({title: e.currentTarget.value}).tooltip('show');
				}
			}
			break;

//		default:
//			console.log(e.code)
		}
	}
	inFocus = false;
	#onFocus = ()=>this.inFocus = true;
	#onBlur = ()=>this.inFocus = false;
}
