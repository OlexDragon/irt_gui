import * as serialPort from '../serial-port.js'
import Controller from './controller.js'
import groupId from '../packet/packet-properties/group-id.js'

export default class ControllerMeasurement extends Controller{

	#$body;

	constructor($card){
		super($card);
		this.#$body = $card.find('.measurement');
	}

	get groupId(){
		return groupId.measurement;
	}

/**
 * @param {Array} payloads
 */
	set update(payloads){

		if(!payloads?.length){
			console.log(packet.toString());
			console.warn('No payloads to parse.');
			serialPort.blink($card, 'connection-wrong');
			return;
		}

		serialPort.blink(this._$card);

		let timeout;
		const rows = [];
		payloads.forEach(pl=>{

			const parameterCode = pl.parameter.code;
			const valId = 'measVal' + parameterCode;
			const descrId = 'measDescr' + parameterCode;
			const $desct = this.#$body.find('#' + descrId);
			const parser = this._parameter.parser(parameterCode);
			if(!parser){
				console.warn('No Parser.')
				return;
			}
			if($desct.length){
				let val = parser(pl.data);
				const isArray = Array.isArray(val);
				const $val = this.#$body.find('#' + valId);
				if(isArray){
					if($val.attr('data-value')!==val.toString())
						$val.attr('data-value', val).html(val.map(v=>{
							v = v.toUpperCase();
							let c;
							if(v.startsWith('UN'))
								c = 'btn btn-outline-danger m-1';
							else if(v.endsWith('LOW') || v === 'MUTE')
								c = 'btn btn-outline-warning m-1';
							else
								c = 'btn btn-outline-success m-1';
							return $('<div>', {class: c, text: v});
						}));
					
				}else if($val.text()!==val)
					$val.text(val);
			}else{
				const showText = this._parameter.name(parameterCode);
				const $row = $('<div>', {class: 'row'});
				const val = parser(pl.data);
				let $v;

				if(showText && showText !== 'Description'){
					$row.append($('<div>', {id: descrId, class: 'col-5', text: showText}));
					$v = $('<div>', {id: valId, class: 'col', text: val});
				}else
					$v =$('<div>', {id: descrId, class: 'col'}).append($('<h4>', {text: val}));

				$row.append($v);

				rows.push($row);
				clearTimeout(timeout);
				timeout = setTimeout(()=>this.#$body.append(rows), 100);
			}
		});
	}
}