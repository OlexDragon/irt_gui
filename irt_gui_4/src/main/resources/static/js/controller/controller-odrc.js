import Controller from './controller.js';
import packetId from '../packet/packet-properties/packet-id.js';
import {code as dlrcCode} from '../packet/parameter/dlrc.js';
import groupId from '../packet/packet-properties/group-id.js';
import ControllerMeasurement from './controller-measurement.js';
import measurement from '../packet/parameter/measurement-odrc.js';
import ControllerLnbBamd from './controller-lnb-band.js'
import { onStartAll } from '../panel-info.js'

export default class ControllerOdrc extends Controller{

	static URL = '/fragment/control/odrc';

	#$odrcMode;
	#$odpcLnb1;
	#$odpcLnb2;
	#bandSelectController;

	#onChangeEvents = [];

	constructor($card){
		super($card);
		this._$card.load(ControllerOdrc.URL, this.#onLoad.bind(this));
		ControllerMeasurement.addIntelligencer(this.#measurementControl.bind(this));
		this.#bandSelectController = new ControllerLnbBamd($card);
		this.#bandSelectController.showOneRow();
		this.#bandSelectController.start();

		onStartAll(yes=>{
			if(!this.#bandSelectController)
				return;
			if(yes)
				this.#bandSelectController.start();
			else
				this.#bandSelectController.stop();
		});
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
					const auto = value.key === 1; // AUTO mode
					this.#$odpcLnb1.prop('disabled', auto);
					if(!this.#$odpcLnb1.prop('checked'))
						this.#$odpcLnb1.next().text('LNB 1');
					this.#$odpcLnb2.prop('disabled', auto);
					if (!this.#$odpcLnb2.prop('checked'))
						this.#$odpcLnb2.next().text('LNB 2');
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
	#onLoad(_, statusText){
		if(statusText !== 'success'){
			console.warn(statusText);
			this.stop();
			return;
		}
		
		this.#$odrcMode = this._$card.find('#odrcMode').change(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcSetMode).attr('data-parameter-code', dlrcCode('Mode Select'));
		this.#$odpcLnb1 = this._$card.find('#lnb1Over').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcLNBSelect).attr('data-parameter-code', dlrcCode('Switchover')).val(1);
		this.#$odpcLnb2 = this._$card.find('#lnb2Over').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.odrcLNBSelect).attr('data-parameter-code', dlrcCode('Switchover')).val(2);
		ControllerMeasurement.addIntelligencer(this.#measurementControl.bind(this));
	}
	#wgs;
	#measurementControl(pls){
		pls.forEach(pl=>{

			const code = pl.parameter.code;

			switch(code){

			case measurement['WGS Status'].code:

				const wgs = measurement['WGS Status'].parser(pl.data);
				const auto = this.#$odrcMode.val() !== '2'; // AUTO mode or NULL
				const disabled = this.#$odpcLnb1.prop('disabled');

				if(this.#wgs === wgs && (disabled === auto))
					return;

				this.#wgs = wgs;
				switch(wgs){

				case 'PROTECTION A':
				case 'LNB 1':{
					this.#$odpcLnb1.prop('disabled', auto).prop('checked', true).next().text('LNB 1 Active');
					this.#$odpcLnb2.prop('disabled', auto).next();
					auto ? this.#$odpcLnb2.next().text('LNB 2') : this.#$odpcLnb2.next().text('Set LNB 2 Active');
					break
				}

				case 'PROTECTION B':
				case 'LNB 2':{

					this.#$odpcLnb2.prop('disabled', auto).prop('checked', true).next().text('LNB 2 Active');
				    this.#$odpcLnb1.prop('disabled', auto).next();
					auto ? this.#$odpcLnb1.next().text('LNB 1') : this.#$odpcLnb1.next().text('Set LNB 1 Active');
					break;
		        }

			   case 'DEFAULT':
			   		this.#$odpcLnb1.prop('checked', false).prop('disabled', auto);
					this.#$odpcLnb2.prop('checked', false).prop('disabled', auto);
				   break;

				default:
					this.#$odpcLnb1.prop('disabled', true).prop('checked', false).next().text('WGS Unavailable');
					this.#$odpcLnb2.prop('disabled', true).prop('checked', false).next().text('WGS Unavailable');
				}
	    	};
		});
	}
}