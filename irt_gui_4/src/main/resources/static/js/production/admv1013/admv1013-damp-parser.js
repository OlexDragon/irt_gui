import DampParser from './damp-parser.js'

export default class Admv1013DampParser extends DampParser{

	constructor(dump) {
		super(128, dump, 1, 2);
	}
}