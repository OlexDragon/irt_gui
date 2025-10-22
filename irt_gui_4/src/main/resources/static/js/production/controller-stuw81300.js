import Controller from './controller.js';
import packetId from '../packet/packet-properties/packet-id.js';
import groupId from '../packet/packet-properties/group-id.js';
import deviceDebug from '../packet/parameter/device-debug.js';
import Register from '../packet/parameter/value/register.js';
import ST0 from './stuw81300/st0.js';
import ST1 from './stuw81300/st1.js';
import ST2 from './stuw81300/st2.js';
import ST3 from './stuw81300/st3.js';
import ST4 from './stuw81300/st4.js';
import ST5 from './stuw81300/st5.js';
import ST6 from './stuw81300/st6.js';
import ST7 from './stuw81300/st7.js';
import ST8 from './stuw81300/st8.js';
import ST9 from './stuw81300/st9.js';
import ST10 from './stuw81300/st10.js';
import ST11 from './stuw81300/st11.js';

export default class STUW81300Controller extends Controller{

	#actionSet;
	#action = {
		packetId: packetId.stuw81300,
		groupId: groupId.deviceDebug,
		function: 'f_reaction',
		f_reaction: this._reaction.bind(this)};

	#$container;
	#ST0;
	#ST1;
	#ST2;
	#ST3;
	#ST4;
	#ST5;
	#ST6;
	#ST7;
	#ST8;
	#ST9;
	#ST10;
	#ST11;

	#$freqSet;
	#$refSet;
	#$R;
	#$N;
	#$FRAC;
	#$DITHERING;
	#$MOD;

	#callback;

	constructor($container, parameterCode, value, packetId, packetIdSet){
		super();
		this.#action.data = {parameterCode: parameterCode, value: value};
		this.#action.packetId = packetId;
		this.#$container = $container;
		$container.load('/fragment/pll/stuw81300', this.#onLoad.bind(this));

		this.#actionSet = Object.assign({}, this.#action);
		this.#actionSet.data = { parameterCode: deviceDebug.readWrite.code};
		this.#actionSet.packetId = packetIdSet;
	}

	get action(){
		return this.#action;
	}
	/**
	 * @param {function(Register)} callback
	 */
	set onSet(callback) {

		if (callback && this.#callback!==callback)
			this.#callback = callback;

		if(!this.#ST11 || this.#ST11.onSet===this.#callback)
			return;

		this.#ST0.onSet = this.#callback;
		this.#ST1.onSet = this.#callback;
		this.#ST2.onSet = this.#callback;
		this.#ST3.onSet = this.#callback;
	    this.#ST4.onSet = this.#callback;
		this.#ST5.onSet = this.#callback;
		this.#ST6.onSet = this.#callback;
		this.#ST7.onSet = this.#callback;
		this.#ST8.onSet = this.#callback;
		this.#ST9.onSet = this.#callback;
		this.#ST10.onSet = this.#callback;
		this.#ST11.onSet = this.#callback;
	}

	_reaction(packet){
		const payloads = packet.payloads;
		this.#ST0.savedValue = payloads;
		this.#ST1.savedValue = payloads;
		this.#ST2.savedValue = payloads;
		this.#ST3.savedValue = payloads;
		this.#ST4.savedValue = payloads;
		this.#ST5.savedValue = payloads;
		this.#ST6.savedValue = payloads;
		this.#ST7.savedValue = payloads;
		this.#ST8.savedValue = payloads;
		this.#ST9.savedValue = payloads;
		this.#ST10.savedValue = payloads;
		this.#ST11.savedValue = payloads;

		this.#freqCalculate();
	}

	#onLoad(){
		const index = this.typeName === 'KA_BIAS' ? ST0.indexBias : ST0.indexConverter;
		this.#ST0 = new ST0(this.#$container.find('#stuw81300-st0'), index);
		this.#ST1 = new ST1(this.#$container.find('#stuw81300-st1'), index);
		this.#ST2 = new ST2(this.#$container.find('#stuw81300-st2'), index);
		this.#ST3 = new ST3(this.#$container.find('#stuw81300-st3'), index);
		this.#ST4 = new ST4(this.#$container.find('#stuw81300-st4'), index);
		this.#ST5 = new ST5(this.#$container.find('#stuw81300-st5'), index);
		this.#ST6 = new ST6(this.#$container.find('#stuw81300-st6'), index);
		this.#ST7 = new ST7(this.#$container.find('#stuw81300-st7'), index);
		this.#ST8 = new ST8(this.#$container.find('#stuw81300-st8'), index);
		this.#ST9 = new ST9(this.#$container.find('#stuw81300-st9'), index);
		this.#ST10 = new ST10(this.#$container.find('#stuw81300-st10'), index);
		this.#ST11 = new ST11(this.#$container.find('#stuw81300-st11'), index);

		this.#$freqSet = this.#$container.find('#freqSet');
		this.#$refSet = this.#$container.find('#refSet');
		this.#$R = this.#$container.find('#R');
		this.#$N = this.#$container.find('#N');
		this.#$FRAC = this.#$container.find('#FRAC');
		this.#$DITHERING = this.#$container.find('#DITHERING');
		this.#$MOD = this.#$container.find('#MOD');

		this.onSet = this.#callback;
	}

	#freqCalculate(){

		const R = parseInt(this.#$R.val());
		const FRAC = parseInt(this.#$FRAC.val());
		const refSet = parseFloat(this.#$refSet.val());
		const D	 = this.#$DITHERING.prop('checked');
		let n = parseInt(this.#$N.val());

		if (isNaN(R) || isNaN(n) || isNaN(FRAC) || isNaN(refSet)){
			this.#$freqSet.val('Impossible to calculate');
			return;
		}

		if (FRAC) {
			const MOD = parseInt(this.#$MOD.val());
			const N_FRAC = FRAC/MOD;
			if(D){
				N_FRAC += 1/(2*MOD);
			}
			n += N_FRAC;
		}
		const FREQUENCY = refSet/R*n;
		this.#$freqSet.val(FREQUENCY);
	}
}