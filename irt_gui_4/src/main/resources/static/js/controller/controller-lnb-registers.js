import * as serialPort from '../serial-port.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import deviceDebug from '../packet/parameter/device-debug.js'
import Register from '../packet/parameter/value/register.js'

export default class ControllerLnbRegisters {

	#action =  {packetId: packetId.lnbRegisters, groupId: groupId.deviceDebug, data: {parameterCode: deviceDebug.readWrite.code}, function: 'f_lnb_registers'};
	#actionSet =  {packetId: packetId.lnbRegistersSet, groupId: groupId.deviceDebug, data: {parameterCode: deviceDebug.readWrite.code}, function: 'f_lnb_registers'};
	#$card;
	#$row;
	#$inputs = [];	// LNB value inputs
	#interval;

	constructor($card) {
		this.#$card = $card;
		if($card.find('.lnb-registers-row').length)
			throw new Error('LNB Registers row already exists in the card.');
		this.#createRow();
		this.start();
		this.#action.f_lnb_registers = this.#reaction.bind(this);
		this.#action.data.value = [new Register(26, 32), new Register(26, 33), new Register(26, 34)];
	}
	start(){
		this.#interval = setInterval(()=>this.readRegisters(), 5000);
	}
	stop(){
		clearInterval(this.#interval);
	}
	readRegisters(){
		console.log('Reading LNB registers...');
		serialPort.postObject(this.#$card, this.#action);
	}
	remove(){
		this.stop();
		if(this.#$row){
			this.#$row.empty();
			this.#$row = undefined;
		}
	}
	#createRow(){
		this.#$row = $('<div>', {class: 'lnb-registers-row row m-3'})

				.append($('<div>', {class: 'col'}).append(this.#addInput()))
				.append($('<div>', {class: 'col'}).append(this.#addInput()))
				.append($('<div>', {class: 'col'}).append(this.#addInput()));

		this.#$row.appendTo(this.#$card);
	}
	#addInput(){
		const count = this.#$inputs.length + 1;
		const $input = $('<input>', {id: `lnb${count}-register-input`, type: 'number', class: 'form-control', placeholder: `LNB #${count} Register value`}).change(this.#sendRegister.bind(this));
        this.#$inputs.push($input);
		const $div = $('<div>', {class: 'form-floating'})
		            .append($input)
					.append($('<label>', {for: `lnb${count}-register-input`, text: `LNB #${count}`}));
		return $div;
	}
	#reaction(packet){
		packet.payloads.forEach(pl=>{
			const reg = Register.parseRegister(pl.data);
			const $input = this.#$inputs.find($el=>$el.prop('id')===`lnb${reg.address - 31}-register-input`);
			const regValue = toRegValue(reg.value);
			if($input.is(":focus")){
				$input.prop('title', `Value: ${regValue.value}, Anable: ${regValue.anable}`);
				return;
			}
			if($input.val() !== `${regValue.value}`){
				$input.val(regValue.value);
				$input.prop('title', `Value: ${regValue.value}, Anable: ${regValue.anable}`);
			}
		});
	}
	#sendRegister({currentTarget:{id, value, blur}}){
		const v = parseInt(value);
        if(isNaN(v) || v<0 || v>0x3F){
			document.activeElement.blur();
            alert('Register value must be between 0 and 63.');
            return;
        }
		const address = parseInt(id.charAt(3)) + 31;
		const toSend = parseInt(value);
		const reg = new Register(26, address, (toSend ? parseInt(value) | 0x100 : toSend));
		this.#actionSet.data.value = reg;
		this.#actionSet.update = true;
		serialPort.postObject(this.#$card, this.#actionSet);
	}
}
function toRegValue(value){
	const v = value & 0x3F;
	const anable = (value & 0x100) >0;
	return {value: v, anable: anable};
}