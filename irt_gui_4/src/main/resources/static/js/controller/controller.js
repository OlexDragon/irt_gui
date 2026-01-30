export default class Controller{

	_parameter;
	#name;

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
     * @param {{}} p
     */
	set parameter(p){
		throw new Error('Setter parameter() must be implemented. Entry: ' + p);
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


	get parameter(){
		return this._parameter;
	}
	/**
	 * @param {{}} p
	 */
	set parameter(p) {
		this._parameter = p;
	}
	stop(){}
	destroy(){
		this._$card.empty();
		this._$card = null;
	}
}