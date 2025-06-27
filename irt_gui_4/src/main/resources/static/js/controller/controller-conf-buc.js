import Controller from './controller.js'
import ControllerValue from '../classes/controller-value.js'
import packetId from '../packet/packet-properties/packet-id.js'
import groupId from '../packet/packet-properties/group-id.js'

export default class ControllerMeasurement extends Controller{
	static url = '/fragment/control/buc';

	#attenuationController;
	#gainController;
	#freqController;
	#$btnMute;
	#$loSelect;
	#toRead;
	#onChangeEvents = [];

	constructor($card) {
		super($card);
		const $body = $card.find('div.control');
		$body.load(ControllerMeasurement.url,()=>{

			const $attenuationTab = $body.click(this.#tabClick.bind(this)).find('#attenuationTab');

			const onValueChange = this.#onValueChange.bind(this);
			this.#attenuationController = new ControllerValue('attenuation', $body.find('div.attenuation'));
			this.#attenuationController.change = onValueChange;

			this.#gainController = new ControllerValue('gain', $body.find('div.gain'));
			this.#gainController.change = onValueChange;

			this.#freqController = new ControllerValue('freq', $body.find('div.frequency'));
			this.#freqController.change = onValueChange;

			this.#$btnMute = $body.find('#btnMute').change(this.#onChangeBtnMute.bind(this));
			this.#$loSelect = $body.find('#loSelect').change(this.#onChangeLoSelect.bind(this));

			const tabCookies = Cookies.get('tabCookies');
			if (tabCookies)
				new bootstrap.Tab($('#' + tabCookies)).show();
			else
				new bootstrap.Tab($attenuationTab).show();
			
		});
	}

	get groupId(){
		return groupId.configuration;
	}

	get toRead(){
		return Object.values(this.#toRead).map(({code})=>code);
	}

	set toRead(v){
		this.#toRead = v;
	}

	/**
	 * @param {Object[]} pls
	 */
	set update(pls){
		if(!this.#gainController){
			console.log('This controller is not ready yet.');
			return;
		}

		pls.sort(({parameter:a},{parameter:b})=>b.code - a.code).forEach(pl=>{

			const pId =this._parameter.default;
			const val = this._parameter.parser(pl.parameter.code)(pl.data);

			switch(pl.parameter.code){

			case pId.attenuationRange.code:
				delete this.#toRead.attenuationRange;
				this.#attenuationController.min = val[1]/10*-1;
				this.#attenuationController.max = val[0];
				this.#attenuationController.step = 0.1;
				break;

			case pId.Attenuation.code:
				{
					const value = val/10;
					this.#attenuationController.value = value;
				}
				break;

			case pId.gainRange.code:
				delete this.#toRead.gainRange;
				this.#gainController.min = val[0]/10;
				this.#gainController.max = val[1]/10;
				this.#gainController.step = 0.1;
				break;

			case pId.Gain.code:
				{
					const value = val/10;
					this.#gainController.value = value;
				}
				break;

			case pId.frequencyRange.code:
				delete this.#toRead.frequencyRange;
				this.#freqController.min = Number(val[0]/1000000n);
				this.#freqController.max = Number(val[1]/1000000n);
				this.#freqController.step = 0.000001;
				break;

			case pId.Frequency.code:
				{
					const value = val/1000000n;
					const remainder = Number(val - value*1000000n)/1000000;
					const result = Number(value)+remainder;
					this.#freqController.value = result;
				}
				break;

			case pId.Mute.code:
				{
					if(val===''){
						this.#$btnMute.attr('disabled', true);
						break;
					}
					const value = val ? 'Unmute' : 'Mute';
					const label = this.#$btnMute.prop('checked', val).attr('disabled', false).next();
					if(label.text()!==value)
						label.text(value);
				}
				break;

			case pId.loSet.code:
				{
					const value = this.#toRead.loSet.parser(pl.data);
					this.#$loSelect.val(value);
				}
				break;

			case pId.LO.code:
				{
					if(!this.#toRead.LO)
						return;

					const value = this.#toRead.LO.parser(pl.data);
					delete this.#toRead.LO;
					if(!pl.parameter.size){
						delete this.#toRead.LO;
						return;
					}

					const potions = value.map((v,i)=>v ? $('<option>', {value: i, text: v}) : undefined).filter(v=>v);
					this.#$loSelect.append(potions).parent().removeClass('visually-hidden');
				}
				break;

//			case pId.Status.code:	// Redundancy
//				console.log(pl);
//				break;

			default:
				console.warn(pl);
			}
		});
	}

	/**
	 * @param {method} e
	 */
	set change(e){
		this.#onChangeEvents.push(e);
	}
	#tabClick({target:{id}}) {
		switch (id) {

		case 'attenuationTab':
			this.#attenuationController.active()
			break;

		case 'gainTab':
			this.#gainController.active()
			break;

		case 'freqTab':
			this.#freqController.active()
			break;

		default:
//			console.warn(id);
			return;
		}
		Cookies.set('tabCookies', id);
	}

	#onValueChange(object) {
		Object.keys(object).forEach(key => {

			let toSend;
			let pId;
			let parameterCode;

			switch (key) {

			case this.#attenuationController.name:
				pId = packetId.atenuationSet;
				parameterCode = this.parameter.default.Attenuation.code;
				toSend = object[key] * 10;
				break;

			case this.#gainController.name:
				pId = packetId.gainSet;
				parameterCode = this.parameter.default.Gain.code;
				toSend = object[key] * 10;
				break;

			case this.#freqController.name:
				pId = packetId.frequencySet;
				parameterCode = this.parameter.default.Frequency.code;
				const value = object[key];
				const floor = Math.floor(value);
				const remainder = Math.round(value % 1 * 1000000);
				toSend = BigInt(floor) * 1000000n + BigInt(remainder);
				break;

			default:
				console.log('To add key = ' + key);
				return;
			}

			this.#sendChange(pId, toSend, parameterCode);
		});
	}

	#onChangeBtnMute(e) {
		const toSend = e.currentTarget.checked ? 1 : 0;	// Mute / Unmute
		this.#sendChange(packetId.muteSet, toSend, this.parameter.default.Mute.code);
	}
	#onChangeLoSelect({currentTarget:{value: toSend}}){
	this.#sendChange(packetId.loSet, toSend, this.parameter.default.loSet.code);
	}

	#sendChange(){
		this.#onChangeEvents.forEach(cb=>cb(...arguments));
	}
}
