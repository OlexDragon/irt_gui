export default class ComControl{

	#elements = {};
	#onChangeEvents = [];

	constructor(...elements){
		elements.forEach($el=>{
			this.#elements['$' + $el.prop('id')] = $el.prop('disabled', true);
			switch($el.prop('nodeName')){
//			case 'INPUT':
//				$el.on('input', this.#onInput);
			default:
				$el.change(this.#onChange);
			}
		});
	}

	disable(){
		elements?.forEach($el=>$el.prop('disabled', true));
	}

	onChange = (e)=>{
		this.#onChangeEvents.push(e);
	}

	get address(){
		const a = this.#elements.$comAddress.val();
		return a && +a;
	}
	set address(v){
		const $comAddress = this.#elements.$comAddress;
		if($comAddress.filter(':focus').length)
			return;

		const vStr = '' + v;
		if($comAddress.val() !== vStr)
			$comAddress.val(vStr);
	}

	get retransmits(){
		const a = this.#elements.$comRetransmits.val();
		return a && +a;
	}
	set retransmits(v){
		const $comRetransmits = this.#elements.$comRetransmits;
		if($comRetransmits.filter(':focus').length)
			return;

		const vStr = '' + v;
		if($comRetransmits.val() !== vStr)
			$comRetransmits.val(vStr);
	}

	get standard(){
		const a = this.#elements.$comStandard.val();
		return a && +a;
	}
	set standard(v){
		const $comStandard = this.#elements.$comStandard;
		if($comStandard.filter(':focus').length)
			return;

		const vStr = '' + v;
		if($comStandard.val() !== vStr)
			$comStandard.val(vStr);
	}

	get baudrate(){
		const a = this.#elements.$comBaudrate.val();
		return a && +a;
	}
	set baudrate(v){
		const $comBaudrate = this.#elements.$comBaudrate;
		if($comBaudrate.filter(':focus').length)
			return;

		const vStr = '' + v;
		if($comBaudrate.val() !== vStr)
			$comBaudrate.val(vStr);
	}

	/**
     * @param {boolean} d
     */
	set disable(d){
		Object.keys(this.#elements).forEach(key=>this.#elements[key].prop('disabled', d));
	}

	#onChange = (e)=>{

		if(!e.currentTarget.value)
			return;

		const v = +e.currentTarget.value;
		let o = {};

		switch(e.currentTarget.id){

		case 'comAddress':
			if(v<0 || v>=255){
				e.currentTarget.value = '';
				e.currentTarget.blur();
				return;
			}
			o.address = v;
			break;

		case 'comRetransmits':
			if(v<0 || v>10){
				e.currentTarget.value = '';
				e.currentTarget.blur();
				return;
			}
			o.retransmit = v;
			break;

		case 'comStandard':
			o.standard = v;
			break;

		case 'comBaudrate':
			o.baudrate = v;
			break;

		default:
			console.warn(e.currentTarget.id);
			return;
		}
		e.currentTarget.blur()
		this.#onChangeEvents.forEach(e=>e(o));
	}
}