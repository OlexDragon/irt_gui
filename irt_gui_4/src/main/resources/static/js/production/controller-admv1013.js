import Controller from './controller.js'
import groupId from '../packet/packet-properties/group-id.js'
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
import deviceDebug from '../packet/parameter/device-debug.js'
import {calMode} from './cal-mode.js'

export default class ADMV1013Controller extends Controller{

	#action = {
		groupId: groupId.deviceDebug,
		function: 'f_reaction',
		f_reaction: this._reaction.bind(this)};

	#actionSet;
	#onSet;

	#$container;
	#$btnSaveAll;
	#$fileInput
	#registers = {};

	constructor($container, parameterCode, value, packetId, packetIdSet){
		super();
		this.#action.data = {parameterCode: parameterCode, value: value};
		this.#action.packetId = packetId;
		this.#$container = $container;
		$container.load('/fragment/pll/admv1013', this.#onLoad.bind(this));

		this.#actionSet = Object.assign({}, this.#action);
		this.#actionSet.data = { parameterCode: deviceDebug.readWrite.code};
		this.#actionSet.packetId = packetIdSet;
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
		this.#registers[0].onSave = this._sendCommand.bind(this);

		this.#registers[1] = new AlarmControl(this.#$container.find('#cardAlarm'));
		this.#registers[1].onSave = this._sendCommand.bind(this);

		this.#registers[2] = new AlarmMaskControl(this.#$container.find('#cardAlarmMask'));
		this.#registers[2].onSave = this._sendCommand.bind(this);

		this.#registers[3] = new EnableControl(this.#$container.find('#cardEnable'));
		this.#registers[3].onSave = this._sendCommand.bind(this);

		this.#registers[5] = new LoAmpIControl(this.#$container.find('#cardLoAnpI'));
		this.#registers[5].onSave = this._sendCommand.bind(this);

		this.#registers[6] = new LoAmpQControl(this.#$container.find('#cardLoAnpQ'));
		this.#registers[6].onSave = this._sendCommand.bind(this);

		this.#registers[7] = new LoOffsetIControl(this.#$container.find('#cardOffsetI'));
		this.#registers[7].onSave = this._sendCommand.bind(this);

		this.#registers[8] = new LoOffsetIControl(this.#$container.find('#cardOffsetQ'));
		this.#registers[8].onSave = this._sendCommand.bind(this);

		this.#registers[9] = new QuadControl(this.#$container.find('#cardQUAD'));
		this.#registers[9].onSave = this._sendCommand.bind(this);

		this.#registers[10] = new TemperatureControl(this.#$container.find('#cardTemperature'));
		this.#registers[10].onSave = this._sendCommand.bind(this);

		this.#$btnSaveAll = this.#$container.find('.admv1013-save').click(this._btnClick.bind(this));

		// Save to file
		this.#$container.find('.save-to-file').click(()=>{

			let fileName = prompt('Enter file name', 'admv1013_registers');
			if (!fileName)
				return;

			fileName = '/gui/' + fileName + '.admv1013';

			const text = Object.entries(this.#registers).map(entry =>{
				const [key,register] = entry;
				return `${key}:${register.value}`;
			}).join('\n');

			console.log(`Saving to file: ${fileName}`);
			$.post('/file/exists', {fileName: fileName}, (exists) => {
				if (exists && !confirm(`File ${fileName} already exists. Overwrite?`))
						return;
				$.post('/file/save', {fileName: fileName, content: text}, (success) => {
					if (success)
						alert(`File ${fileName} saved successfully.`);
					else
						alert(`Error saving file ${fileName}.`);
				})
				.fail(error=>{
					console.error(error.responseText);
					alert(`Error saving file ${fileName}.`);
				});
			});
		});

		// Load from file
		this.#$container.find('#fileInput').change(({target:fileInput}) => {
			const file = fileInput.files[0];
			if (!file)
				return;
			const reader = new FileReader();
			reader.onload = (e) => {
				const content = e.target.result.split(/\r?\n/);
				setRegisterValue(this.#registers, content);
			};
			fileInput.value = '';
			reader.readAsText(file);
		});
	}

	_reaction(regs){
		this.#$btnSaveAll.prop('disabled', !calMode)
		regs.forEach(r => {
			if (this.#registers[r.address])
				this.#registers[r.address].register = r;
		});
	}

	_sendCommand(reg, disableFlash){

		this.#actionSet.data.value = [reg];
		this.#actionSet.update = true;
		this.#onSet(this.#actionSet);

		if (disableFlash)
			return;

		const rSave = new Register(reg.index, 0x10, 1);
		console.log('Scheduling flash save for register', rSave);
		setTimeout(() => {
			this.#actionSet.data.value = [rSave];
			this.#actionSet.update = true;
			this.#onSet(this.#actionSet);
		}, 500);
	}

	_btnClick(){
		return confirm('Are you sure you want to save the changes?');
	}
}

// Recursively set register values from the list
function setRegisterValue(registerControls, values){
	if (!values.length)
		return;
	const line = values.splice(0,1);
	const [key, value] = line[0].split(':');
	const register = registerControls[key];
	const reg = register.register;
	if (!reg){
		console.warn(`Register with key ${key} not found.`);
		return;
	}
	reg.value = parseInt(value);
	register.register = reg;
	register.reset();
	register.save();
	setTimeout(()=>setRegisterValue(registerControls, values), 1000);
}
