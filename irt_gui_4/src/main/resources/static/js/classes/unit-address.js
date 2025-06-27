import {showToast} from '../serial-port.js'

export default class UnitAddress{

	#$unitAddress;

	constructor($unitAddress){
		this.#$unitAddress = $unitAddress.change(this.#onChange);
		const ua = Cookies.get('unitAddress');
		if(ua){
			$unitAddress.val(ua);
			this._unotAddress = ua;
		}else
			$unitAddress.val(254);
	}

	get unitAddress(){
		return +this.#$unitAddress.val();
	}

	set unitAddress(address){
		clearTimeout(this._timeout);

		if(typeof address ==='string')
			address = +address;
		if(Array.isArray(address))
			if(address.length)
				address = address[0];
			else
				return;

		const value = +this.#$unitAddress.val();
		if(value === address)
			return;

		if(address<0 || address>=255){
			showToast('Address error', 'The address value cannot be a negative number or exceed 254.', 'text-bg-danger bg-opacity-50');
			return;
		}

		this.#$unitAddress.val(address).change();
	}

	#onChange = (e) =>{
		Cookies.set('unitAddress', e.currentTarget.value, {expires: 365, path: ''});
	}
}