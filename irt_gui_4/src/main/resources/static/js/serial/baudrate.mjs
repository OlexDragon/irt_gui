// baudrate.mjs

export default class Baudrate {

    #element;

    #storageKey;

    constructor(element, {
        storageKey = 'unit',
        defaultBaudrate = '115200'
    } = {}) {

        this.#element = element;

        this.#storageKey = `baudrate:${storageKey}`;

        this.#element.addEventListener(
            'change',
            ({ currentTarget: { value } }) => this.#onChange(value)
        );

        this.#element.value =
            localStorage.getItem(this.#storageKey)
            ?? defaultBaudrate;

    }

    get baudrate() {

        return +this.#element.value;

    }

    set baudrate(value) {

        this.#element.value = value.toString();

        this.#onChange(value);

    }

    toggleClass(classToToggle, status) {

        this.#element.classList.toggle(classToToggle, status);

    }

    #onChange(value) {

        localStorage.setItem(this.#storageKey, value);

    }

}