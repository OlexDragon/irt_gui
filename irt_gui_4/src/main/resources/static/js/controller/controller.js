export default class Controller{

	#parametersClass;
	#name;
	_toRead;

	constructor($card){
		this._$card = $card.find('div.control');
	}

	get name(){
		return this.#name;
	}
	set name(name){
		if(this.#name){
			console.error('A name can only be defined once.')
			return;
		}

		this.#name = name;
	}

	/**
     * @param {[]} pls
     */
	set update(pls){
		throw new Error('Setter parameter() must be implemented. Entry: ' + pls);
	}

/**
 * @param {Array} payloads
 */
	set update(payloads){
		throw new Error('Setter parameter() must be implemented. Entry: ' + payloads);
	}

	/**
	 * @param {{}} p
	 */

	get parametersClass(){
		return this.#parametersClass;
	}
	/**
	 * @param {{}} p
	 */
	set parametersClass(p) {
		this.#parametersClass = p;
		this._toRead = p.all;
	}
	get toRead(){
		if(!this._toRead)
			return;
		return Object.values(this._toRead).map(({code})=>code);
	}
	stop(){}
	destroy(){
		this._$card.empty();
		this._$card = null;
	}
}