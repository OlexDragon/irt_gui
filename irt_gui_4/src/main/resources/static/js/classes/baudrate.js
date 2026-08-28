export default class Baudrate {

    #baudrateElement;
    #storageKey;

    constructor(baudrateElement, { storageKey = 'unit', defaultBaudrate = '115200' } = {}) {

        this.#baudrateElement = baudrateElement;
        this.#storageKey = storageKey + ':Baudrate';

        this.#baudrateElement.addEventListener(
            'change',
            ({ currentTarget: { value } }) => this.#onChange(value)
        );

        this.#baudrateElement.value = localStorage.getItem(this.#storageKey) ?? defaultBaudrate;
    }

    get baudrate() {
        return +this.#baudrateElement.value;
    }

    set baudrate(value) {
        this.#baudrateElement.value = value.toString();
        this.#onChange(value);
    }

    #onChange(value) {
        localStorage.setItem(this.#storageKey, value);
    }
}