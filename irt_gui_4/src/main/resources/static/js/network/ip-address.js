export default class IpAddress{
	#bytes;

	constructor(bytes){
		if(bytes && bytes.length!=13){
			console.log(bytes);
			throw new Error('The byte length is incorrect!');
		}
		this.#bytes = bytes ? bytes : Array.from(Array(13));
	}

	get bytes(){
		return this.#bytes;
	}

	set bytes(bytes){
		if(bytes && bytes.length!=13)
			throw new Error('The byte length is incorrect!');
		this.#bytes = bytes ? bytes : Array.from(Array(13));		
	}

	get type(){
		return this.#bytes[0];
	}
	/**
	 * @param {string | number} v
     */
	set type(v){
		if(typeof v === 'string')
			v = parseInt(v);
		this.#bytes[0] = v&0xff;
	}

	get address(){
		return this.#bytes.slice(1,5);
	}
	/**
     * @param {string | number[]} v
     */
	set address(v){
		this.#fillBytes(v, 'address');
	}

	get mask(){
		return this.#bytes.slice(5, 9);
	}
	/**
     * @param {string | number[]} v
     */
	set mask(v){
		this.#fillBytes(v, 'mask');
	}

	get gateway(){
		return this.#bytes.slice(9, 13);
	}
	/**
     * @param {string | number[]} v
     */
	set gateway(v){
		this.#fillBytes(v, 'gateway');
	}

	equals(o){
		return this.bytes.toString() === o.bytes.toString();
	}

	toString(){
		return networkTypes[this.type&3] + '\n' +
				this.address?.join('.') + '\n' +
				this.mask?.join('.') + '\n' +
				this.gateway?.join('.');
	}
	/**
	 * @param {string | number[]} v
	 */

	#fillBytes(bytes, start){

		if(typeof bytes ==='string')
			bytes =  stringToBytes(bytes);

		if(typeof start === 'string')
			start = keys.indexOf(start);

		let x = start * 4 + 1;
		for(let i=0; i<5 && i<bytes.length; ++i, ++x){
			this.#bytes[x] = bytes[i]
		}
	}
}

const networkTypes = ['Unknown', 'Static', 'Dynamic']
const keys = ['address', 'mask', 'gateway']

function stringToBytes(v){
	if(typeof v === 'string')
		return v.split('.').map(v=>parseInt(v));
	return v;
}
