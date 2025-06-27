import IpRow from './ip-row.js'
import IpAddress from './ip-address.js'

export default class NetworkControl{

	#typeSelect;
	#address;
	#mask;
	#gateway;
	#btnOk;
	#btnCansel;
	#btnHttp;
	#savedValue;	// Saved value
	#onChangeEvents = [];
	#onNotSaved = [];

	constructor($body){

		this.#typeSelect = $body.find('#selectNetworkType').attr('disabled', true).on('input', this.#onInput).change(this.#onChange);
		this.#typeSelect.attr('name' , 'type');
		this.#btnOk = $body.find('#btnVetworkOk').attr('disabled', true).click(this.#onOk);
		this.#btnCansel = $body.find('#btnNetworkCancel').attr('disabled', true).click(this.#onCansel);
		this.#btnHttp = $body.find('#btnHttp').attr('disabled', true);

		this.#gateway	 = new IpRow($body.find('#networkGateway'))	.onInput(this.#onInput).disable(true).onNext(this.#btnOk);
		this.#gateway.name = 'gateway';
		this.#mask		 = new IpRow($body.find('#networkMask'))		.onInput(this.#onInput).disable(true).onNext(this.#gateway);
		this.#mask.name = 'mask';
		this.#address	 = new IpRow($body.find('#networkAddress')).onInput(this.#onInput).disable(true).onNext(this.#mask);
		this.#address.name = 'address';
		
	}

	get value(){
		const type = this.#typeSelect.val();
		const v = new IpAddress();
		v.type = type
		v.address = this.#address.value;
		v.mask = this.#mask.value;
		v.gateway = this.#gateway.value;
		return v;
	}
	set value(v){

		if(v){

			let ipAddress;
			if(Array.isArray(v))
				ipAddress = new IpAddress(v);
			else
				ipAddress = v;
			const value = this.value;
			if( !this.#value || this.#value.equals(value)){

				if(value.equals(ipAddress))
					return;

				this.#btnHttp.prop('disabled', false).attr('href', `http://${ipAddress.address.join('.')}`);

				this.#typeSelect.val(ipAddress.type).attr('disabled', false);
				this.#address.value = ipAddress.address;
				this.#mask.value = ipAddress.mask;
				this.#gateway.value = ipAddress.gateway;
				this.#disable(ipAddress.type);
				this.#value = this.value;

			}else{

				const o = {};
				o.currentValue = this.#value;
				o.toSave = value;
				this.#onNotSaved.forEach(e=>e(o));
			}
			return ;

		}else{

			const type = this.#typeSelect.val();
			const v = new IpAddress(type, this.#address.value, this.#mask.value, this.#gateway.value);
		}
	}

	onChange = e =>{
		this.#onChangeEvents.push(e);
		return this;
	}

	onNotSaved = e =>{
		this.#onNotSaved.push(e);
		return this;
	}

	disable(){
		this.#typeSelect.prop('disabled', true);
		this.#btnOk.prop('disabled', true);
		this.#btnCansel.prop('disabled', true);

		this.#address.disable(disable);
		this.#mask.disable(disable);
		this.#gateway.disable(disable);
	}

	get #value(){
		return this.#savedValue;
	}
	set #value(v){
		this.#savedValue = v;
		this.#btnOk.prop('disabled', true);
		this.#btnCansel.prop('disabled', true);
	}
	#disable = v =>{
		const disable = v == '2';
		this.#address.disable(disable);
		this.#mask.disable(disable);
		this.#gateway.disable(disable);
	}

	#onInput = e =>{
		const name = e.currentTarget.name;
		const value = e.currentTarget.value;
		let disable = this.#value[name].toString() === value.toString();
		this.#btnOk.prop('disabled', disable);
		this.#btnCansel.prop('disabled', disable);
	}

	#onChange = e =>{
		this.#disable(e.currentTarget.value);
		this.#address.focus();
	}

	#onOk = () =>{
		this.#value = this.value;
		this.#onChangeEvents.forEach(e=>e(this.#value));
	}

	#onCansel = () =>{
		const value = this.#value
		this.#value = this.value;
		this.value = value;
	}
}