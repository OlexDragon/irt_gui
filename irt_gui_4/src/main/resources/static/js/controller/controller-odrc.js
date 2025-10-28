import Controller from './controller.js';
import packetId from '../packet/packet-properties/packet-id.js';
import {code as dlrcCode} from '../packet/parameter/dlrc.js';
import groupId from '../packet/packet-properties/group-id.js';
import ControllerMeasurement from './controller-measurement.js';
import measurement from '../packet/parameter/measurement-odrc.js';

export default class ControllerOdrc extends Controller{

	static URL = '/fragment/control/odrc';

	#$odrcMode;
	#$odpcLnb1;
	#$odpcLnb2;

	#onChangeEvents = [];

	constructor($card){
		super($card);
		const $body = $card.find('.control');
		$body.load(ControllerOdrc.URL, ()=>{

			this.#$odrcMode = $('#odrcMode').change(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcSetMode).attr('data-parameter-code', dlrcCode('Mode Select'));
			this.#$odpcLnb1 = $('#odpcLnb1').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcLNBSelect).attr('data-parameter-code', dlrcCode('Switchover')).val(1);
			this.#$odpcLnb2 = $('#odpcLnb2').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcLNBSelect).attr('data-parameter-code', dlrcCode('Switchover')).val(2);
		});
		ControllerMeasurement.addIntelligencer(this.#measurementControl.bind(this));
	}

	get groupId(){
		return groupId.configuration;
	}

	/**
     * @param {Array} pls
     */
	set update(pls){
		pls.forEach(pl=>{

			const c = pl.parameter.code;
			const name = this._parameter.name(c);

			switch(name){

			case 'Switchover':	// Write only
			case 'Status':		// Deprecated
				break;

			case 'Mode Select':{
					const parser = this._parameter.parser(name);
					const value = parser(pl.data);
					if (+this.#$odrcMode.val() === value.key)
						break;
					this.#$odrcMode.val(value.key);
					this.#$odrcMode.prop('disabled', false);
					this.#$odpcLnb1.prop('disabled', value.key === 1); // AUTO mode);
					this.#$odpcLnb2.prop('disabled', value.key === 1); // AUTO mode);
					break;
				}
				

			default:
//				console.warn(name, pl);
			}
		});
	}

	/**
     * @param {Method} e
     */
	set change(e){
		this.#onChangeEvents.push(e);
	}

	#onCange = ({currentTarget:{value, dataset:{packetid, parameterCode}}})=>{
		this.#wgs = undefined; // force update of WGS status
		this.#$odpcLnb1.prop('disabled', true); // disable while waiting for status update
		this.#$odpcLnb2.prop('disabled', true); // disable while waiting for status update
		this.#onChangeEvents.forEach(cb=>cb(+packetid, +value, +parameterCode));
	}
	#wgs;
	#measurementControl(pls){
		pls.forEach(pl=>{

			const code = pl.parameter.code;

			switch(code){

			case measurement['WGS Status'].code:

				const wgs = measurement['WGS Status'].parser(pl.data);

				if (this.#wgs === wgs)
					return;

				this.#wgs = wgs;
				switch(wgs){

				case 'LNB 1':{
					const auto = this.#$odrcMode.val() === '1'; // AUTO mode
					this.#$odpcLnb1.prop('disabled', auto).prop('checked', true).next().text('LNB 1 Active');
					this.#$odpcLnb2.prop('disabled', auto).next().text('Set LNB 2 Active');
					break
				}
 
               case 'LNB 2':{
				const auto = this.#$odrcMode.val() === '1'; // AUTO mode
					this.#$odpcLnb2.prop('disabled', auto).prop('checked', true).next().text('LNB 2 Active');
				    this.#$odpcLnb1.prop('disabled', auto).next().text('Set LNB 1 Active');
					break;
			   }

				default:
					this.#$odpcLnb1.prop('disabled', true).prop('checked', false).next().text('WGS Unavailable');
					this.#$odpcLnb2.prop('disabled', true).prop('checked', false).next().text('WGS Unavailable');
				}
	    	};
		});
	}
}