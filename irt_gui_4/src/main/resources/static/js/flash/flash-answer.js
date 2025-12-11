export default class FlashAnswer{
	static names = ['NULL', 'ACK', 'NACK'];
	static code = [0, 0x79, 0x1f];

	constructor(code){
		this.code = code&0xff;
	}

	get name(){
		const index = FlashAnswer.code.indexOf(this.code);
		if(index>=0)
			return FlashAnswer.names[index];

		return 'UNKNOWN';
	}
}