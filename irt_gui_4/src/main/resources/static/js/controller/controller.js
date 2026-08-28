export default class Controller {

    #parametersClass;
    #name;
    _toRead;
    _$card
    _card;
    _root;

    constructor(card) {
        this._root = card instanceof Element ? card : card[0];
		this._card = this._root.querySelector('div.control');
		this._$card = $(this._card);
    }

    _onLoad() {
    }

    get name() {
        return this.#name;
    }
    set name(name) {
        if (this.#name) {
            console.error('A name can only be defined once.');
            return;
        }

        this.#name = name;
    }

    /**
     * @param {Array} payloads
     */
    set update(payloads) {
        throw new Error(
            `Setter update() must be implemented. Entry: ${payloads}`
        );
    }

    /**
     * @param {{}} p
     */
    get parametersClass() {
        return this.#parametersClass;
    }
    /**
     * @param {{}} p
     */
    set parametersClass(p) {
        this.#parametersClass = p;
        this._toRead = p.readAllCode;
    }

    get toRead() {
//        console.log('[get toRead()]', { _toRead: this._toRead });
        if (!this._toRead == null)
            return;
        return Object.values(this._toRead);
    }

	start(){}
    stop() {}

    destroy() {
        if (this._card) {
            this._card.innerHTML = '';
            this._card = null;
        }
    }
}