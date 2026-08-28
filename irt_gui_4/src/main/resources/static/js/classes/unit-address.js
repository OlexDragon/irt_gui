import { showToast } from '../serial-port.js'

export default class UnitAddress {

    #unitAddressElement;

    constructor(unitAddressElement) {
        this.#unitAddressElement = unitAddressElement;
        this.#unitAddressElement.addEventListener('change', this.#onChange);

        const ua = Cookies.get('unitAddress');
        if (ua) {
            this.#unitAddressElement.value = ua;
            this._unotAddress = ua;
        } else {
            this.#unitAddressElement.value = '254';
        }
    }

    get unitAddress() {
        return +this.#unitAddressElement.value;
    }

    set unitAddress(address) {
        clearTimeout(this._timeout);

        if (typeof address === 'string')
            address = +address;
        if (Array.isArray(address))
            if (address.length)
                address = address[0];
            else
                return;

        const value = +this.#unitAddressElement.value;
        if (value === address)
            return;

        if (address < 0 || address >= 255) {
            showToast('Address error', 'The address value cannot be a negative number or exceed 254.', 'text-bg-danger bg-opacity-50');
            return;
        }

        this.#unitAddressElement.value = address;
        this.#unitAddressElement.dispatchEvent(new Event('change'));
    }

    #onChange = (e) => {
        Cookies.set('unitAddress', e.currentTarget.value, { expires: 365, path: '' });
    }
}