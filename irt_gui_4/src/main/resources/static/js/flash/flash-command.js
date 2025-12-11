class FlasCommand{

	#name;
	#bytes;
	constructor(name, bytes){
		this.#name = name;
		this.#bytes = bytes;
	}

	get name(){
		return this.#name;
	}
	get command(){
		return this.#bytes;
	}
}

const commands = [
	new FlasCommand('EMPTY'			, []),
	new FlasCommand('CONNECT'		, [0x7F]),
	new FlasCommand('GET'			, [0x00, 0xFF]),	// 0x00 - command; 0xFF - checksum]
	new FlasCommand('GET_VERSION'	, [0x01, 0xFE]),
	new FlasCommand('GET_ID'		, [0x02, 0xFD]),
	new FlasCommand('READ_MEMORY'	, [0x11, 0xEE]),
	new FlasCommand('WRITE_MEMORY'	, [0x31, 0xCE]),
	new FlasCommand('ERASE'			, [0x43, 0xBC]),
	new FlasCommand('EXTENDED_ERASE', [0x44, 0xBB])
];

export default commands;
export function getCommand(name){
	return commands.find(fc=>fc.name===name).command;
}
