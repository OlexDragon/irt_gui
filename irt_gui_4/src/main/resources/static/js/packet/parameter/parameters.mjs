
import translator from '../../helper/text-translator.mjs';

export default class Parameters {

    #parameters;
    #name;
    #byCode;
    #translationPrefix;

    constructor(parameters, name, translationPrefix = 'measurement') {

        this.#name = name;
        this.#translationPrefix = translationPrefix;

        this.#addNameIfNeeded(parameters);

        this.#parameters = Object.freeze(parameters);

        this.#byCode = [];

        for (const parameter of Object.values(parameters)) {
            this.#byCode[parameter.code] = parameter;
        }
    }

    get parameters() {
        return this.#parameters;
    }

    toCode(name) {
        if (typeof name === 'number')
            return name;

        return this.#parameters[name].code;
    }

    parser(value) {
        return this.#byCode[this.toCode(value)]?.parser;
    }

    toName(code) {

        const parameter = this.#byCode[code];

        if (!parameter)
            return;

        return parameter.name;
    }

    translation(code) {

        const key = `${this.#translationPrefix}.${code}`;
        const translated = translator.translate(key);

        if (key !== translated)
            return translated;

        console.warn(`The key "${key}" is missing`);

        return this.toName(code);

    }

    toString(value) {
        const c = this.toCode(value);
        return `${this.#name}: ${this.toName(c)} (${c})`;
    }

    get readAllCode() {
        return { readAll: 255 };
    }

    #addNameIfNeeded(parameters) {
        for (const [key, parameter] of Object.entries(parameters)) {
            parameter.name ??= key;
        }
    }
}