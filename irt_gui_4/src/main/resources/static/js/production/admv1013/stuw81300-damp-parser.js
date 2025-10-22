import DampParser from './damp-parser.js'

export default class Stuw81300DampParser extends DampParser{

	constructor(dump) {
		super(127, dump, 2, 3);
	}
}