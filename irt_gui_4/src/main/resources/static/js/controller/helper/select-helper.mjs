// select-helper.mjs

export default class SelectHelper {

    #root;
    #select;
    #onChange;
    #callback;

    constructor(select, callback) {
        this.#select = select.localName === 'select' ? select : select.querySelector('select');
        this.#root = select.parentElement;
        this.#callback = callback;
        this.#onChange = () => this.#doCallback();
        this.#select.addEventListener('change', this.#onChange);
    }

    get id() {
        return this.#select.id;
    }

    get value() {
        return this.#select.value;
    }

    set value(value) {
        this.#select.value = value;
    }

    get disabled() {
        return this.#select.disabled;
    }

    /**
     * @param {boolean} disabled
     */
    set disabled(disabled) {
        this.#select.disabled = disabled;
    }

    fill(values, selected = '', { createDefault = false, defaultText = 'Select' } = {}) {

        let options = [];
        if (createDefault) {
            const option = new Option(defaultText, '');
            option.disabled = true;
            option.hidden = true;
            options.push(option);
        }

        this.#select.replaceChildren(
            ...options,
            ...values.map(({ value, name }) => new Option(name, value, false, selected === value || selected === name))
        );
        if (this.#select.value)
            this.#doCallback();
    }

	toggleClass(classToToggle, status){
		this.#select.classList.toggle(classToToggle, status);
	}

    #doCallback() {
        this.#callback?.({
            id: this.#select.id,
            value: this.#select.value,
        });
    }
	
    show() {
        this.#root.classList.remove('visually-hidden');
        this.disabled = false;
    }

    hide() {
        this.#root.classList.add('visually-hidden');
        this.disabled = true;
    }

    #destroyed = false;
    destroy() {
        if (this.#destroyed)
            return;
        this.#select.removeEventListener('change', this.#onChange);
        this.#destroyed = true;
    }
}