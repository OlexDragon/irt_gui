import {switchoverMode, standbyMode, status, switchover, name, parser} from '../packet/parameter/irpc.js'
import {update} from '../panel-control.js'

export default class ControllerMeasurement{
	#$controllerStatus;
	#$unitsStatus;

	#$fields = {};

	constructor($controllerStatus, $unitsStatus){
		this.#$controllerStatus = $controllerStatus;
		this.#$unitsStatus = $unitsStatus;
	}

	/**
     * @param {any[]} pls
     */
	set update(pls){
		pls.forEach(pl=>{

			const c = pl.parameter.code;
			const key = name(c);

			switch(c){

			case standbyMode:
			case switchoverMode:
				update({[key]: pl.data});
				break;

			case status:
				{
					const d = parser(c)([...pl.data]);
					update({[key]: d});
					const {status, bucStatus} = d;
					Object.keys(status).forEach(key=>{
						const text = status[key];
						this.#setValue('flag-',key, text);
					});
					this._fillUnitsStatus(bucStatus);
				}
				break;
		
			case switchover:
				break;
			}
		});
	}

	_createRow(name){

		const $div = $('<div>', {class: 'col'});
		this.#$controllerStatus.append($('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: name})).append($div));
		return $div;
	}

	_fillUnitsStatus(bucStatus){

		// Show BUC's Names
		const keys = Object.keys(bucStatus);
		if(!this.#$fields.bucs){
			this.#$fields.bucs = {};
			this.#$fields.rows = {};
			const $names = $('<div>', {class: 'row mt-2'});
			this.#$unitsStatus.append($names.append($('<div>', {class: 'col text-bg-info opacity-50'})));
			keys.forEach(key=>$names.append($('<div>', {class: 'col text-bg-info opacity-50', text: key})));
		}

		const bucs = this.#$fields.bucs;
		const rows = this.#$fields.rows;
		keys.forEach(key=>{
			const buc = bucs[key] ?? (bucs[key] = {});
			const status = bucStatus[key].status;
			this.#setBucValue(buc, rows, status);
		});
	}

	#getBucRow(rows, key){

		if(!rows[key]){
			rows[key] = $('<div>', {class: 'row'});
			this.#$unitsStatus.append(rows[key]);
		}
		return rows[key];
	}

	#setBucValue(buc, rows, bucStatus){
		Object.keys(bucStatus).forEach(key=>{

			if(!rows[key])
				this.#getBucRow(rows, key).append($('<div>', {class: 'col text-truncate text-bg-info opacity-50', text: key}));

			if(!buc[key]){
				buc[key] = $('<div>', {class: 'col', text: bucStatus[key]});
				rows[key].append(buc[key]);
			}else
				if(buc[key].text() !== bucStatus[key])
					buc[key].text(bucStatus[key]);
		});
	}

	#setValue(prefix, key, text){
		const k = prefix + key;
		const $div = this.#$fields[k] || (this.#$fields[k] = this._createRow(key));
		$div.text() !== text && $div.text(text);
	}
}
