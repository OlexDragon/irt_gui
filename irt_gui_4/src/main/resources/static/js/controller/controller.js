export default class Controller{

	_parameter;

	constructor($card){
		this._$card = $card;
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
}