// controller-measurement.js

import Controller from './controller.js'
import groupId from '../packet/packet-properties/group-id.mjs'
import { translate } from '../packet/service/converter.js'

export default class ControllerMeasurement extends Controller {
  static #intelligencer = [];
  static addIntelligencer(callBack) {
    ControllerMeasurement.#intelligencer.push(callBack);
  }

  #bodyElement;
  #pendingRows = [];
  #debounceTimer = null;

  constructor(card) {
    super(card);
    this.#bodyElement = this._root.querySelector('.measurement');
  }

  get groupId() {
    return groupId.measurement;
  }

  /**
     * @param {object[]} payloads
     */
  set update(payloads) {
    ControllerMeasurement.#intelligencer.forEach(cb => cb(payloads));

    payloads.forEach(pl => this.#processPayload(pl));
  }

  #processPayload(pl) {
    const code = pl.parameter.code;
    const parser = this.parametersClass.parser(code);

    if (!parser || parser === 'do not show') {
      if (!parser) console.warn('No Parser for parameterCode ', pl);
      return;
    }

    const descrElement = document.getElementById('measDescr' + code);
    const val = parser(pl.data);

    if (val === undefined) return;

    if (descrElement) {
      this.#updateExistingValue(code, val);
    } else {
      this.#createNewRow(code, val);
    }
  }

  #updateExistingValue(code, val) {
    const valElement = document.getElementById('measVal' + code);
    if (!valElement) return;

    if (Array.isArray(val)) {
      this.#renderStatusBadges(val, valElement);
    } else if (valElement.textContent !== String(val)) {
      valElement.textContent = val;
    }
  }

  #renderStatusBadges(values, container) {
    const key = values.toString();
    if (container.getAttribute('data-value') === key) return;

    container.setAttribute('data-value', key);
    container.innerHTML = '';

    values.forEach(v => {
      if (typeof v !== 'string') {
        console.warn('Something went wrong.');
        return;
      }

      const status = v.toUpperCase();
      const className = this.#getBadgeClass(status);
      
      const badge = document.createElement('div');
      badge.className = className;
      badge.textContent = translate('measurement.status', status);
      container.appendChild(badge);
    });
  }

  #getBadgeClass(status) {
    if (status.startsWith('UN')) return 'btn btn-outline-danger m-1';
    if (status.endsWith('LOW') || status === 'MUTE') return 'btn btn-outline-warning m-1';
    return 'btn btn-outline-success m-1';
  }

  #createNewRow(code, val) {
    const showText = this.parametersClass.translation(code);
    const name = this.parametersClass.toName(code);
    
    const row = document.createElement('div');
    row.className = 'row';

    const hasLabel = showText && name !== 'Description';
    const descrId = 'measDescr' + code;
    const valId = 'measVal' + code;

    if (hasLabel) {
      const label = document.createElement('div');
      label.id = descrId;
      label.className = 'col-5';
      label.textContent = showText;
      row.appendChild(label);
    }

    const valueContainer = document.createElement('div');
    valueContainer.id = hasLabel ? valId : descrId;
    valueContainer.className = 'col';

    if (hasLabel) {
      valueContainer.textContent = val;
    } else {
      const heading = document.createElement('h4');
      heading.textContent = val;
      valueContainer.appendChild(heading);
    }

    row.appendChild(valueContainer);
    this.#queueRow(row);
  }

  #queueRow(row) {
    this.#pendingRows.push(row);
    
    clearTimeout(this.#debounceTimer);
    this.#debounceTimer = setTimeout(() => {
      const fragment = document.createDocumentFragment();
      this.#pendingRows.forEach(r => fragment.appendChild(r));
      this.#bodyElement.appendChild(fragment);
      this.#pendingRows = [];
    }, 100);
  }
}