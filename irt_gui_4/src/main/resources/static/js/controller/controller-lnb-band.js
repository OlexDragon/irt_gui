import * as serialPort from '../serial-port.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'
import { code } from '../packet/parameter/dlrc.js'

export default class ControllerLnbBamd{

	#action;
	#actionSet;
	#$card;
	#$row1;
	#$row2;
	#interval;
	#showOneRow;

	constructor($card) {
		this.#$card = $card;
		this.lnb1Code = code('LNB1 Band Select');
		this.lnb2Code = code('LNB2 Band Select');
		this.#action =  {packetId: packetId.lnbBand, groupId: groupId.configuration, data: {parameterCode: [this.lnb1Code, this.lnb2Code]}, function: 'f_lnb_bamd', f_error: this.#packetError.bind(this)};
		this.#actionSet =  {packetId: packetId.lnbBandSet, groupId: groupId.configuration, data: {parameterCode: code('LNB1 Band Select')}, function: 'f_lnb_bamd', f_error: this.#packetError.bind(this)};

		if($card.find('.lnb-band-row').length)
			throw new Error('LNB Registers row already exists in the card.');
	}
	start(){
		if(!this.#$row1){
			this.#createRows();
			this.#action.f_lnb_bamd = this.#actionSet.f_lnb_bamd = this.#reaction.bind(this);
		}
		clearInterval(this.#interval);
		this.#run();
		this.#interval = setInterval(this.#run.bind(this), 6000);
	}
	stop(){
		clearInterval(this.#interval);
	}
	destroy(){
		this.stop();
		if(!this.#$row1){
			this.#$row1.remove();
			this.#$row2.remove();
		}
		this.#$card = null;
		this.#$row1 = null;
		this.#$row2 = null;
	}
	showOneRow(){
		this.#showOneRow = true;
		if(!this.#$row2)
			return;
		this.#$row2.addClass('visually-hidden');
		this.#$row1.find(':first-child').text('LNB Band');
	}
	#createRows(){
		const text = this.#showOneRow ? 'LNB Band' : 'LNB1 Band';
		this.#$row1 = $('<div>', {class: 'lnb1 lnb-band-row row m-3'})

				.append($('<div>', {class: 'col align-self-center', text: text}))
				.append($('<div>', {class: 'col'}).append(this.#addCheckBox('lnbBand1', 'lnb1Band', 'Low', this.lnb1Code, 1)))
				.append($('<div>', {class: 'col'}).append(this.#addCheckBox('lnbBand2', 'lnb1Band', 'High', this.lnb1Code, 2)));

		if(!this.#showOneRow)
			this.#$row2 = $('<div>', {class: 'lnb2 lnb-band-row row m-3'})

				.append($('<div>', {class: 'col align-self-center', text: 'LNB2 Band'}))
				.append($('<div>', {class: 'col'}).append(this.#addCheckBox('lnbBand3', 'lnb2Band', 'Low', this.lnb2Code, 1)))
				.append($('<div>', {class: 'col'}).append(this.#addCheckBox('lnbBand4', 'lnb2Band', 'High', this.lnb2Code, 2)));

		this.#$card.append([this.#$row1, this.#$row2]);
	}
	#addCheckBox(id, name, text, code, value){
		return [
			$('<input>', {type: 'radio', class: `btn-check ${text}`, name: name, id: id, autocomplete: 'off', 'data-code': code, value: value}).change(this.#change.bind(this)),
			$('<label>', {class: 'btn ', for: id, text: text})
		];
	}
	#change({currentTarget:{dataset:{code}, value}}){
//		console.log(code, value);
		this.#actionSet.data.parameterCode = +code;
		this.#actionSet.data.value = +value;
		this.#actionSet.update = true;
		serialPort.postObject(this.#$card, this.#actionSet);
	}
	#reaction(packet){
		if(!this.#$row1)
			return;
		packet.payloads.forEach(pl=>{
			const d = pl.data[0]
			if(!d){
				clearInterval(this.#interval);
				this.#$row1.addClass('visually-hidden');
				this.#$row2.addClass('visually-hidden');
				return;
			}
			switch(pl.parameter.code){

			case this.lnb1Code:
				this.#setStatus(this.#$row1, d);
				break;

			case this.lnb2Code:
				if(this.#$row2)
					this.#setStatus(this.#$row2, d);
				break;
			}
		});
	}
	#run(){
		if(!serialPort.doRun()){
			this.stop();
			return;
		}
		if(this.#action.buisy){
			console.log('Buisy')
			return
		}
		serialPort.postObject(this.#$card, this.#action);
	}
	#setStatus($row, value){

		switch(value){

		case 1:
			$row.find('input.Low').prop('checked', true);
			break;

		case 2:
			$row.find('input.High').prop('checked', true);
			break;
		}
	}
	#packetError(packet){
		if(packet.header.error === 10){	// Requested element not foundr
			console.warn('The Packet has an error. Controller stops.\n', packet.toString());
			this.stop();
			if(this.#$row1)
				this.#$row1.addClass('visually-hidden');
			if(this.#$row2)
				this.#$row2.addClass('visually-hidden');
		}else
			console.warn(packet.toString());
	}
}