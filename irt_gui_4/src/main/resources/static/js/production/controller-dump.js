import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'

export default class DumpController extends Controller{

	#action = {
		packetId: packetId.dumpHelp,
		groupId: groupId.deviceDebug,
		data: {
			parameterCode: deviceDebug.debugInfo.code,
			 value: 100
		 },
		function: 'f_reaction',
		f_reaction: this.#reaction.bind(this)};

	#onSet;

	#$container;
	#$btnDumpGet
	#$dumpResult;
	#$dumpHelp;

	constructor($container){
		super();
		this.#$container = $container;
		$container.load('/fragment/dump/dump', this.#onLoad.bind(this));
	}

	get action(){
		if(this.#action.packetId!==packetId.dumpHelp)
			this.#action.doNotSend = this.#action.data.value === undefined;
		return this.#action;
	}

	/**
     * @param {string} n
     */
	set name(n){
		super.name = n;
		this.#action.name = n;
	}

	/**
     * @param {(action: any) => void} cb
     */
	set onSet(cb){
		this.#onSet = cb;
	}

	#onLoad(){
		this.#$container.find('input[name=dumpSelect]').change(this.#onChenge.bind(this));
		this.#$container.find('#dumpAddress').change(this.#addrChenge.bind(this));

		this.#$btnDumpGet = this.#$container.find('#btnDumpGet').click(this.#sendGet.bind(this));
		this.#$dumpResult = this.#$container.find('#dumpResult');
		this.#$dumpHelp = this.#$container.find('#dumpHelp');
	}

	#reaction(packet){

		switch(packet.header.packetId){

		case  packetId.dumpHelp:
			packet.payloads.forEach(pl=>{
				const text = deviceDebug.debugInfo.parser(pl.data);
				this.#$dumpHelp.val(text);
				this.#action.packetId = packetId.dump;
				this.#action.data.value = undefined;
			});
			break;

		case  packetId.dump:
			packet.payloads.forEach(pl=>{
				const text = deviceDebug.debugInfo.parser(pl.data);
				if(this.#$dumpResult.val()!==text)
					this.#$dumpResult.val(text);
			});
			break

		default:
			console.warn(packet.header.packetId, packet);
		}
	}

	#sendGet(){
		this.#onSet(this.#action);
	}

	#onChenge({currentTarget: {value}}){
		this.#$dumpResult.val('');
		this.#action.data.parameterCode = +value;
		this.#action.update = true;
		this.#sendGet();
	}

	#addrChenge({currentTarget: {value}}){
		const disable = value === '';
		this.#$dumpResult.val('');
		this.#$btnDumpGet.prop('disabled', disable);
		this.#action.data.value = disable ? undefined : +value;
		if(disable)
			return;
		this.#action.update = true;
		this.#sendGet();
	}
}

