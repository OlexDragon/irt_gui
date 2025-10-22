import Register from '../../packet/parameter/value/register.js'

export default class DampParser{

	#dump;
	#index;
	#addrIndex;
	#valueIndex;

	constructor(index, dump, addrIndex, valueIndex) {
		this.#index = index;
		this.#dump = dump;
		this.#addrIndex = addrIndex;
		this.#valueIndex = valueIndex;
	}

	parse() {
		const registers = [];
		this.#dump.split('\n').map(line=>line.trim().split(/(\s+)/).map(s=>s.trim()).filter(Boolean)).filter(split=>split.length>this.#valueIndex)
		.map(split=>{
            const addr = parseInt(split[this.#addrIndex].replace(/[():x]/g, ''), 16);
			const value = parseInt(split[this.#valueIndex].split('x')[1], 16);
			registers.push(new Register(this.#index, addr, value));
		});
		return registers;
	}
}