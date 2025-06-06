export default class ControllerIrpc{

	static URL = '/fragment/control/irpc';

	#$salectSwtchHvr;
	#$salectStndBy;
	#$btnIrspDefault;
	#$btnHoverA;
	#$btnHoverB;

	#onChangeEvents = [];

	constructor($body){

		$body.load(ControllerIrpc.URL, ()=>{

			this.#$salectSwtchHvr = $('#salectSwtchHvr').change(this.#onCange);
			this.#$salectStndBy = $('#salectStndBy').change(this.#onCange);
			this.#$btnIrspDefault = $('#btnIrspDefault').click(this.#onCange);
			this.#$btnHoverA = $('#btnHoverA').click(this.#onCange);
			this.#$btnHoverB = $('#btnHoverB').click(this.#onCange);
		});
	}

	/**
     * @param {{}} object
     */
	set update(object){
		Object.keys(object).forEach(key=>{
			const value = object[key];

			switch(key){

			case 'Switchover Mode':
				this.#$salectSwtchHvr.val(value[0]);
				break;

			case 'Standby Mode':
				this.#$salectStndBy.val(value[0]);
				break;

			case 'Status':

				const status = value.bucStatus["Unit S"].status.Status;

				switch(status){

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
				}}
				break;

			default:
				console.warn(object);
			}
		});
	}

	onChange(event){
		this.#onChangeEvents.push(event);
	}
	#onCange = (e)=>{
		const value = {[e.currentTarget.id]: +e.currentTarget.value};
		this.#onChangeEvents.forEach(e=>e(value));
	}
}