import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'
import SpiControl from './admv1013/spi-control.js'
import AlarmControl from './admv1013/alarm-controller.js'
import AlarmMaskControl from './admv1013/alarm-mask-control.js'
import EnableControl from './admv1013/enable-control.js'
import LoAmpIControl from './admv1013/lo-amp-i-control.js'
import LoAmpQControl from './admv1013/lo-amp-q-control.js'
import LoOffsetIControl from './admv1013/lo-offset-control.js'
import QuadControl from './admv1013/quad-control.js'
import TemperatureControl from './admv1013/temperature-control.js'
import {calMode} from './cal-mode.js'

export default class ADMV1013sController extends Controller{

	#action = {
		packetId: packetId.admv1013,
		groupId: groupId.deviceDebug,
		data: {
			parameterCode: deviceDebug.readWrite.code,
			 value: [
				new Register(7,0),
				new Register(7,1),
				new Register(7,2),
				new Register(7,3),
				new Register(7,5),
				new Register(7,6),
				new Register(7,7),
				new Register(7,8),
				new Register(7,9),
				new Register(7,10)
			]},
		function: 'f_reaction',
		f_reaction: this.#reaction.bind(this)};

	#actionSet = Object.assign({}, this.#action);
	#onSet;

	#$container;
	#$btnSaveAll;
	#registers = {};

	constructor($container){
		super();
		this.#$container = $container;
		$container.load('/fragment/admv/admv1013', this.#onLoad.bind(this));
		this.#actionSet.packetId = packetId.admv1013Set;
	}

	get action(){
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

		this.#registers[0] = new SpiControl(this.#$container.find('#cardSpiControl'));
		this.#registers[0].onSave = this.#sendCommand.bind(this);

		this.#registers[1] = new AlarmControl(this.#$container.find('#cardAlarm'));
		this.#registers[1].onSave = this.#sendCommand.bind(this);

		this.#registers[2] = new AlarmMaskControl(this.#$container.find('#cardAlarmMask'));
		this.#registers[2].onSave = this.#sendCommand.bind(this);

		this.#registers[3] = new EnableControl(this.#$container.find('#cardEnable'));
		this.#registers[3].onSave = this.#sendCommand.bind(this);

		this.#registers[5] = new LoAmpIControl(this.#$container.find('#cardLoAnpI'));
		this.#registers[5].onSave = this.#sendCommand.bind(this);

		this.#registers[6] = new LoAmpQControl(this.#$container.find('#cardLoAnpQ'));
		this.#registers[6].onSave = this.#sendCommand.bind(this);

		this.#registers[7] = new LoOffsetIControl(this.#$container.find('#cardOffsetI'));
		this.#registers[7].onSave = this.#sendCommand.bind(this);

		this.#registers[8] = new LoOffsetIControl(this.#$container.find('#cardOffsetQ'));
		this.#registers[8].onSave = this.#sendCommand.bind(this);

		this.#registers[9] = new QuadControl(this.#$container.find('#cardQUAD'));
		this.#registers[9].onSave = this.#sendCommand.bind(this);

		this.#registers[10] = new TemperatureControl(this.#$container.find('#cardTemperature'));
		this.#registers[10].onSave = this.#sendCommand.bind(this);

		this.#$btnSaveAll = this.#$container.find('.admv1013-save').click(this.#btnClick.bind(this));
	}

	#reaction(packet){
		this.#$btnSaveAll.prop('disabled', !calMode)
		packet.payloads.forEach(pl=>{
			const r = Register.parseRegister(pl.data);
			this.#registers[r.address].register = r; 
		});
	}

	#sendCommand(reg){
		this.#actionSet.data.value = reg;
		this.#actionSet.update = true;
		this.#onSet(this.#actionSet);
	}

	#btnClick(){
		if(!confirm('Are you sure you want to save the changes?'))
			return;

		this.#sendCommand(new Register(20, 5, 1));
	}
}

