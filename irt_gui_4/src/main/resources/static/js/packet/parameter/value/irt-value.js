
export default class IrtValue {

    #value;
    #divider;

    constructor(value, prefix, postfix, divider = 1) {
        this.#value = value;
        this.prefix = prefix;
        this.postfix = postfix;
        this.#divider = divider;
    }

    get value() {
        return typeof this.#value === 'number' ? this.#value / this.#divider : this.#value;
    }

    valueOf() {
        return this.value;
    }

    toString() {
        return `${this.prefix ?? ''}${this.value}${this.postfix ?? ''}`;
    }
}
