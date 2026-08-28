// value-panel.js
import UserSettings from '../helper/user-settings.mjs';

export default class ValuePanel {

    _name;
    _value;
    _fields = {};

    dragCount = 0;
    #inFocus = false;

    #onChangeEvents = [];

    constructor(body) {
        this._name = body.dataset.controller;
        this._body = body;

        const range = body.querySelector('input.form-range');
        range.addEventListener('input', this.#rangeOnInput);
        range.addEventListener('change', this.#rangeOnChange);
        this._fields.range = range;

        const input = body.querySelector('input.control');
        input.addEventListener('keydown', this.#inputOnKeydown);
        input.addEventListener('change', this.#inputOnChange);
        input.addEventListener('focus', this.#onFocus);
        input.addEventListener('input', e => this.#onInpot(e));
        input.addEventListener('blur', this.#onBlur);
        this._fields.input = input;

        const stepEl = body.querySelector('input.step');
        stepEl.addEventListener('change', this.#stepOnChange);
        //        stepEl.setAttribute('data-input-name', `${this._name}-step`);
        this._fields.step = stepEl;

        if (!(this._fields.range &&
            this._fields.input &&
            this._fields.step))
            throw new Error(`${this._name}: There cannot be empty fields.`);

        const rangeInput = this._fields.range;
        const setInput = this._fields.input;
        let min = rangeInput.min;
        if (!min) {
            min = 0;
            rangeInput.setAttribute('min', min);
        }

        const max = rangeInput.max;
        if (!max) {
            rangeInput.setAttribute('max', 100);
        }

        const step = rangeInput.step;
        if (!step)
            rangeInput.setAttribute('step', 1);

        const val = setInput.value;
        if (!val) {
            setInput.value = min;
            rangeInput.value = min;
        } else {
            const value = parseFloat(val)
            if (value < min) {
                setInput.value = min;
                rangeInput.value = min;
            } else if (value > max) {
                setInput.value = max;
                rangeInput.value = max;
            } else
                rangeInput.value = value;
        }

        this.#valueFromStorage();
    }

    /**
    * @param {callback} onChange
    */
    set change(onChange) {
        this.#onChangeEvents.push(onChange);
    }

    get value() {
        return +this._fields.input.value;
    }

    set value(value) {

        if (typeof value !== 'number' || isNaN(value))
            throw new Error(`Value must be a number. Received: ${value}`);

        const firstTime = !this._value;
        this._value = value;

        if (this.dragCount || this.#inFocus || value === Number(this._fields.input.value))
            return;

        this._fields.input.value = value;
        this._fields.range.value = value;
        this.#showTooltip(firstTime)
    }

    get min() {
        return +this._fields.range.min;
    }
    set min(newMin) {

        //		if(this._name === 'gainOffset')
        //			debugger;
        if (typeof newMin === 'string')
            newMin = parseInt(newMin);

        const minRange = parseInt(this._fields.range.min);
        if (newMin === minRange)
            return minRange;

        this._fields.range.setAttribute('min', newMin);
        const max = parseInt(this._fields.range.max);
        if (max <= newMin)
            this._fields.range.max = newMin + 100;

        return newMin;
    }

    get max() {
        return +this._fields.range.max;
    }
    set max(newMax) {

        if (typeof newMax === 'string')
            newMax = parseInt(newMax);

        const maxRange = parseInt(this._fields.range.max);
        if (newMax === maxRange)
            return newMax;

        this._fields.range.max = newMax;
        return newMax;
    }

    active = () => {
        this.#showTooltip();
    };

    #isVisible() {
        return this._fields.range.offsetParent !== null;
    }

    tickMarks(set) {
        if (!set?.size)
            return;

        const listName = this._name + '-ticks';
        let $tickList;
        if (this._fields.range.list)
            $tickList = $('#' + listName).empty();
        else {
            $tickList = $('<datalist>', { id: listName }).insertAfter(this._fields.range);
            this._fields.range.setAttribute('list', listName)
        }

        const array = Array.from(set);
        const options = array.map(v => $('<option>', { value: v }));
        $tickList.append(options);
        this._fields.range.setAttribute("step", array[1] - array[0]);
    }

    step(newStep) {
        if (newStep) {
            if (typeof newStep == 'string')
                newStep = parseFloat(newStep);

            const stepRange = parseFloat(this._fields.range.step);
            if (newStep == stepRange)
                return stepRange;

            this._fields.range.setAttribute('step', newStep);
            const stepStr = newStep.toString();
            this.doToFixed = stepStr.includes('.') ? stepStr.split('.')[1].length : undefined;
        }

        return parseFloat(this._fields.range.step);
    }
    userStep() {
        return Number(this._fields.step.value)
    }

    disable() {
        Object.values(this._fields).forEach(el => {
            el.disabled = true;
        });
    }

    hideTooltip() {
        const tooltip = bootstrap.Tooltip.getInstance(this._fields.range);
        tooltip?.hide();
    }

    #tooltip(value) {
        return bootstrap.Tooltip.getOrCreateInstance(this._fields.range, {
            title: String(value),
            //	        trigger: 'manual'
        });
    }

    #rangeOnInput = (e) => {
        const tool = document.getElementsByClassName('tooltip-inner');
        if (tool.length)
            tool[0].textContent = e.currentTarget.value;
        this.dragCount++;
    }

    #showTooltip(firstTime = false) {

        if (!this.#isVisible())
            return;

        const value = Number(this._fields.range.value);
        const tooltip = this.#tooltip(value);
        tooltip.setContent({
            '.tooltip-inner': String(value)
        });

        if (firstTime) // Wait until the page elements stop changing their size, then the tooltip will be in the correct place.
            return;

        tooltip.show();
    }

    #rangeOnChange = (e) => {

        let value;
        const stepStr = this._fields.step.value;
        const rangeStr = e.currentTarget.value;

        if (stepStr && this.dragCount == 1) {

            const step = parseFloat(stepStr);
            value = Number(this._fields.input.value);
            const range = parseFloat(rangeStr);

            if (range < value)
                value = value - step;
            else
                value = value + step;

            this._fields.range.value = value;
            value = this._fields.range.value;
        } else
            value = e.currentTarget.value

        this._fields.input.value = value;
        this.#showTooltip();
        this._sendChange(value);
        this.dragCount = 0;
    }

    #inputOnChange = ({ currentTarget: el }) => {
        const rVal = Number(this._fields.range.value);
        let value = +el.value;
        if (value !== rVal) {

            const step = this._fields.step.value || this._fields.range.step;
            if (step) {
                if ((rVal - value) < 0)
                    value = rVal + (+step);
                else
                    value = rVal - step;
                el.value = value;

            }
        }
        this._fields.range.value = value;
        const v = Number(this._fields.range.value);

        if (this._value !== v) {
            this.#showTooltip();
            this._sendChange(v);
        }

        if (rVal === v)
            el.value = v;
    }

    #stepOnChange = ({ currentTarget }) => {
        const value = Number(currentTarget.value);

        if (!value) {
            currentTarget.value = '';
            return;
        }

        const minStep = Number(this._fields.range.step);

        if (value < minStep)
            currentTarget.value = minStep;

        this.#valueToStorage({ currentTarget });
    }

    #inputOnKeydown = (e) => {

        const { value: targetValue } = e.currentTarget;
        switch (e.code) {

            case 'ArrowDown':
                {
                    const step = this._fields.step.value;
                    if (step) {
                        e.preventDefault();
                        const value = targetValue - parseFloat(step);
                        this._fields.range.value = value;
                        e.currentTarget.value = this._fields.range.value;
                        this.#showTooltip({ value: targetValue });
                    }
                }
                break;

            case 'ArrowUp':
                {
                    const step = this._fields.step.value;
                    if (step) {
                        e.preventDefault();
                        const value = Number(targetValue) + Number(step);
                        this._fields.range.value = value;
                        e.currentTarget.value = this._fields.range.value;
                        this.#showTooltip();
                    }
                }
                break;

            case 'Escape':
                const escape = this._escape();
                if (escape) {
                    e.currentTarget.value = escape;
                    this.#onInpot(e);
                }
                break;

            default:
//                console.log(e.code)
        }
    }

    #storageKey(elementiD) {
        return `value-panel:${this._name}:${elementiD}`;
    }

    #onInpot({ currentTarget: { value } }) {
        this._fields.range.value = value;
    }
    #onFocus = () => this.#inFocus = true;
    #onBlur = () => this.#inFocus = false;

    #valueFromStorage() {
        Object.values(this._fields).forEach(el => {

            const { id } = el;
            if (!id)
                return;

            const value = UserSettings.get(this.#storageKey(id));
            if (value)
                el.value = value;
        });
    }

    #valueToStorage({ currentTarget: { id, value } }) {
        UserSettings.set(this.#storageKey(id), value);
    }

    _sendChange(toSend) {
        this.#onChangeEvents.forEach(e => e({ [this._name]: toSend }));
    }
}
