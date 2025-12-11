import Controller from './controller.js';
import packetId from '../packet/packet-properties/packet-id.js';
import {code as dlrcCode} from '../packet/parameter/dlrc.js';
import groupId from '../packet/packet-properties/group-id.js';
import ControllerMeasurement from './controller-measurement.js';
import measurement from '../packet/parameter/measurement-odrc.js';

export default class ControllerLnb extends Controller{

	static URL = '/fragment/control/lnb';

	#$lnbMode;
	#$lnb1Over;
	#$lnb2Over;
	#measurementCode = measurement['WGS Status'].code;
	#measurementParser = measurement['WGS Status'].parser;

	#onChangeEvents = [];

	constructor($card){
		super($card);
		const $body = $card.find('.control');
		$body.load(ControllerLnb.URL, ()=>{

			this.#$lnbMode = $('#lnbMode').change(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.lnbSetMode).attr('data-parameter-code', dlrcCode('Mode Select'));
			const switchover = dlrcCode('Switchover');
			this.#$lnb1Over = $('#lnb1Over').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.lnbOverSet).attr('data-parameter-code', switchover).attr('data-default-value', 11).val(12);
			this.#$lnb2Over = $('#lnb2Over').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.lnbOverSet).attr('data-parameter-code', switchover).attr('data-default-value', 11).val(13);
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
					if (+this.#$lnbMode.val() === value.key)
						break;
					this.#$lnbMode.val(value.key);
					this.#$lnbMode.prop('disabled', false);

					const auto = value.key === 1; // AUTO mode
					this.#$lnb1Over.prop('disabled', auto);
					if(!this.#$lnb1Over.prop('checked'))
						this.#$lnb1Over.next().text('Protection LNB 1');
					this.#$lnb2Over.prop('disabled', auto);
					if (!this.#$lnb2Over.prop('checked'))
						this.#$lnb2Over.next().text('Protection LNB 2');
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

	#onCange = ({currentTarget:{value, dataset:{packetid, parameterCode, setDefault, defaultValue}}})=>{
		const v = setDefault ? defaultValue : value;
		this.#wgs = undefined; // force update of WGS status
		this.#$lnb1Over.prop('disabled', true); // disable while waiting for status update
		this.#$lnb2Over.prop('disabled', true); // disable while waiting for status update
		this.#onChangeEvents.forEach(cb=>cb(+packetid, +v, +parameterCode));
	}
	#wgs;
	#measurementControl(pls){
		pls.filter(pl=>pl.parameter.code===this.#measurementCode).forEach(pl=>{

			const wgs = this.#measurementParser(pl.data);

			if (this.#wgs === wgs)
				return;

			this.#wgs = wgs;
			switch(wgs){

			case 'PROTECTION LNB 1':{
				const auto = this.#$lnbMode.val() === '1'; // AUTO mode
				this.#$lnb1Over.prop('disabled', auto).removeAttr('data-set-default').prop('checked', true).next().text('Over LNB 1');
				auto ? this.#$lnb2Over.prop('disabled', false) : this.#$lnb2Over.prop('disabled', auto);
				this.#$lnb2Over.attr('data-set-default', true).next().text('Set Default');
				break
			}

						
			case 'PROTECTION LNB 2':{
				const auto = this.#$lnbMode.val() === '1'; // AUTO mode
				this.#$lnb2Over.prop('disabled', auto).removeAttr('data-set-default').prop('checked', true).next().text('Over LNB 2');
			    this.#$lnb1Over.prop('disabled', auto);
				auto ? this.#$lnb1Over.prop('disabled', false) : this.#$lnb1Over.prop('disabled', auto);
				this.#$lnb1Over.attr('data-set-default', true).next().text('Set Default');
				break;
		    }

			case 'data-set-default':
			case 'DEFAULT':
				const auto = this.#$lnbMode.val() === '1'; // AUTO mode
		   		this.#$lnb1Over.prop('checked', false).prop('disabled', auto).removeAttr('data-set-default').next().text('Protection LNB 1');
				this.#$lnb2Over.prop('checked', false).prop('disabled', auto).removeAttr('data-set-default').next().text('Protection LNB 2');
				break;

			default:
				this.#$lnb1Over.prop('disabled', true).prop('checked', false).removeAttr('data-set-default').next().text('Unavailable');
				this.#$lnb2Over.prop('disabled', true).prop('checked', false).removeAttr('data-set-default').next().text('Unavailable');
			}
		});
	}
}