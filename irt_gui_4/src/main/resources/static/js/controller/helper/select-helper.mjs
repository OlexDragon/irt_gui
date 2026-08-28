// select-helper.mjs
export default class SelectHelper {

    #root;
    #select;
    #onChange;

    constructor(select, callback) {
        this.#select = select.localName === 'select' ? select : select.querySelector('select');
        this.#root = select.parentElement;
        this.#onChange = () => {
            callback?.({
                id: this.#select.id,
                value: this.#select.value,
            })
        };
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

    fill(values) {
        this.#select.replaceChildren(
            ...values.map(({ value, name }) => new Option(name, value))
        );
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