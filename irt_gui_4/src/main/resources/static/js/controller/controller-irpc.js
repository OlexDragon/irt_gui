import Controller from './controller.js'
import packetId from '../packet/packet-properties/packet-id.js'
import {code as irpcCode} from '../packet/parameter/irpc.js'
import groupId from '../packet/packet-properties/group-id.js'

export default class ControllerIrpc extends Controller{

	static URL = '/fragment/control/irpc';

	#$salectSwtchHvr;
	#$salectStndBy;
	#$btnIrspDefault;
	#$btnHoverA;
	#$btnHoverB;

	#onChangeEvents = [];

	constructor($card){
		super($card);
		const $body = $card.find('.control');
		$body.load(ControllerIrpc.URL, this.#onLoad.bind(this));
	}

	get groupId(){
		return groupId.redundancy;
	}

	/**
     * @param {Array} pls
     */
	set update(pls){
		if(!this.#$salectSwtchHvr)
			return;
		
		pls.forEach(pl => {

			const c = pl.parameter.code;
			const key = this.parametersClass.toName(c);
			const parser = this.parametersClass.parser(c);
			const value = parser(pl.data);

			switch (key) {

				case 'Standby Mode':
					this.#$salectStndBy.val(pl.data[0]).prop('disabled', false);
					break;

				case 'Switchover Mode':
					this.#$salectSwtchHvr.val(pl.data[0]&3).prop('disabled', false);
					break;

				case 'Status':
					{
						const status = value.bucStatus["Unit S"].status.Status;
						switch (status) {

						case 'Standby':
							{
								const disableA = value.bucStatus["Unit A"].status.Operational !== 'Yes';
								const disableB = value.bucStatus["Unit A"].status.Operational !== 'Yes';
								this.#$btnIrspDefault.prop('disabled', true);
								this.#$btnHoverA.prop('disabled', disableA);
								this.#$btnHoverB.prop('disabled', disableB);
							}
							break;

						default:
							{
								const automatic = value.status["Switchover Mode"] === 'Automatic';
								const disable = (value.bucStatus["Unit A"].status.Operational !== 'Yes' || value.bucStatus["Unit B"].status.Operational !== 'Yes') && automatic;
								this.#$btnIrspDefault.prop('disabled', disable);
								this.#$btnHoverA.prop('disabled', automatic);
								this.#$btnHoverB.prop('disabled', automatic);
							}
						}
					}
				break;

				case 'Switchover':
//					console.log(parser(pl.data));
					break;

				default:
					console.log(key);
			}
		});
	}

	/**
     * @param {Method} e
     */
	set change(e){
		this.#onChangeEvents.push(e);
	}

	disable(){
		this.#$salectSwtchHvr.prop('disabled', true);
		this.#$salectStndBy.prop('disabled', true);
		this.#$btnIrspDefault.prop('disabled', true);
		this.#$btnHoverA.prop('disabled', true);
		this.#$btnHoverB.prop('disabled', true);
	}

	#onLoad(_, statusText){
		if(statusText !== 'success'){
			console.warn(statusText);
			return;
		}
		this.#$salectSwtchHvr = this._$card.find('#irpcSalectSwtchHvr').change(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.irpcSalectSwtchHvr).attr('data-parameter-code', irpcCode('Switchover Mode'));
		this.#$salectStndBy = this._$card.find('#irpcStandBy').change(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.irpcStandBy).attr('data-parameter-code', irpcCode('Standby Mode'));
		this.#$btnIrspDefault = this._$card.find('#irpcDefault').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.irpcDefault).attr('data-parameter-code', irpcCode('Switchover'));
		this.#$btnHoverA = this._$card.find('#irpcHoverA').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.irpcHoverA).attr('data-parameter-code', irpcCode('Switchover'));
		this.#$btnHoverB = this._$card.find('#irpcHoverB').click(this.#onCange).prop('disabled', true).attr('data-packetId', packetId.irpcHoverB).attr('data-parameter-code', irpcCode('Switchover'));
	}
	#onCange = ({currentTarget:{value, dataset:{packetid, parameterCode}}})=>{
		this.disable();
		this.#onChangeEvents.forEach(cb=>cb(+packetid, +value, +parameterCode));
	}
}