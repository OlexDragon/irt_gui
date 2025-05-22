export default class IpRow{

	#inputs;
	#onNext;
	#onInputEvents = [];

	constructor($row){

		this.name = $row.prop('id');

		this.#inputs = $row.find('input');
		this.#inputs.each((_, el)=>{
			el.addEventListener('input', this.#onInput);
			el.addEventListener('change', this.#onChange);
			el.addEventListener('keydown', this.#onKeydown);
			el.addEventListener('focus', this.#onFocus);
			document.addEventListener('paste', this.#onPaste);
			document.addEventListener('copy', this.#onCopy);
		});
	}

	disable = disabled => {

		if(disabled===undefined)
			return this.#inputs.prop('disabled');

		this.#inputs.attr('disabled', disabled);
		return this;
	}

	get value(){
		return this.#inputs.map((_,el)=>el.value ? parseInt(el.value) : undefined).get();
	}
	set value(v){
		if(!v)
			return this.value();

		if(typeof v === 'string')
			v = v.split('.');

		this.#setValue(v);
	}

	focus = () => this.#focusNext();

	onNext(e){
		this.#onNext = e;
		return this;
	}

	onInput(e){
		this.#onInputEvents.push(e);
		return this;
	}

	#setValue = v =>{
		v.forEach((v,i)=>{
			const el = this.#inputs.get(i)
			el.value=v;
			const o = {currentTarget: el};
			this.#onChange(o);
		});
	}

	#onInput = e =>{

		if(e.currentTarget.value.includes('.')){
			this.#setValue(e.currentTarget.value.split('.'));
		}else if(e.currentTarget.value.length>=3){
//			this.#onChange(e);
			this.#focusNext(e);
		}

		const o = {};
		o.currentTarget = {};
		o.currentTarget = this;
		this.#onInputEvents.forEach(e=>e(o));
		return this;
	}

	#onChange = e =>{
		const value = parseInt(e.currentTarget.value);
		const byte = value&0xff;
		if(value!=byte){
			e.currentTarget.value = byte;
			if(!e.currentTarget.classList.contains('border-danger'));
				e.currentTarget.classList.add('border-danger');
		}else
			e.currentTarget.classList.remove('border-danger');
	}

	#onCopy = e =>{

		const target = this.#inputs.filter((_,el)=>el===e.target);
		if(!target.length)
			return;

		const ip = this.#inputs.map((_,el)=>el.value).get().join('.')
		e.clipboardData.setData("text/plain", ip);
		e.preventDefault();
	}

	#onPaste = e =>{

		const target = this.#inputs.filter((_,el)=>el===e.target);
		if(!target.length)
			return;

		const o = {};
		o.currentTarget = {};
		o.currentTarget.value = (event.clipboardData || window.clipboardData).getData("text");

		if(o.currentTarget.value.includes('.')){
			e.preventDefault();
			this.#onInput(o);
		}
	}

	#onKeydown = e =>{
		switch(e.code){

		case 'NumpadDecimal':
		case 'Period':
		case 'Enter':
		case 'NumpadEnter':

			e.preventDefault();
			this.#focusNext(e);

			break;

		default:
		}
	}

	#onFocus = e =>e.currentTarget.select();

	#focusNext(e){

		if(this.disable()){
			this.#onNext?.focus();	
			return;
		}

		let  index
		if(!e)
			index = 0;
		else{
			index = Math.max(this.#inputs.map((i,el)=>el===e.currentTarget ? i : -1).filter((_,i)=>i>=0).get());
			++index;
		}
		if(this.#inputs.length>index)
			this.#inputs.get(index).focus();

		else this.#onNext?.focus();
	}
}
