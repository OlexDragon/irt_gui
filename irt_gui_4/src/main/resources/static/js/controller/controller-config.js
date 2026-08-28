// controller-config.js
import Controller from './controller.js'
import ControllerValue from './controller-value.js'
import packetId, { info as infoId } from '../packet/packet-properties/packet-id.mjs'
import {info as infoGroup} from '../packet/packet-properties/group-id.mjs'
import IrtValue from '../packet/parameter/value/irt-value.js';
import CheckboxHelper from './helper/checkbox-helper.mjs';
import SelectHelper from './helper/select-helper.mjs';
import translator from '../helper/text-translator.mjs';
import { info as infoType } from '../packet/packet-properties/packet-type.js'

export default class ControllerConfig extends Controller {
    static url = '/fragment/control/buc';

    #onChangeEvents = [];

    tabs = {};
    controllers = {};
    elements = {};

    constructor(card) {
        super(card);
        // Load the fragment
        fetch(ControllerConfig.url)
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.text();
            })
            .then(html => {
                super._onLoad();
                this._card.innerHTML = html;
                this.#onLoad();
            })
            .catch(error => {
                console.warn('Error loading config:', error);
            });
    }

    get groupId() {
        return infoGroup.configuration;
    }

    /**
     * @param {Object[]} pls
     */
    set update(pls) {

        if (this._toRead.readAll)
            this.#replaceReadAll(pls);
        if (!this.controllers.loSet) {
            console.log('This controller is not ready yet.');
            return;
        }

        pls.sort(({ parameter: a }, { parameter: b }) => b.code - a.code).forEach(pl => {
            const { code } = pl.parameter
            const val = this.parametersClass.parser(code)(pl.data);
            const name = this.parametersClass.toName(code);

            const value = val instanceof IrtValue
                ? val.value
                : val;
            const ps = this.parametersClass.parameters;

            switch (name) {

                case ps.alcLevelRange?.name:
                case ps.attenuationRange.name:
                case ps.gainOffsetRange?.name:
                case ps.gainRange.name:
                    delete this._toRead[name];
                    const controllerName = name.replace('Range', '');
                    this.tabs[controllerName].closest('.nav-item').classList.remove('visually-hidden');
                    this.controllers[controllerName].min = val[0] / 10;
                    this.controllers[controllerName].max = val[1] / 10;
                    this.controllers[controllerName].step = 0.1;
                    break;

                case ps.alcLevel?.name:
                case ps.attenuation.name:
                case ps.gainOffset?.name:
                case ps.gain.name: {
                    this.controllers[name].value = value;
                    const text = this.tabs[name].textContent;
                    const split = text.split(' : ');
                    this.tabs[name].textContent = split[0] + ' : ' + value;
                    break;
                }

                case ps.frequencyRange.name:
                    delete this._toRead.frequencyRange;
                    if (val.filter(v => v).length) {
                        this.tabs.frequency.classList.remove('visually-hidden');
                        this._frequencyRange(val.map(v => Number(v / 1000000n)));
                    } else {
                        delete this._toRead.frequency;
                        this.tabs.frequency.classList.add('visually-hidden');
                    }
                    break;

                case ps.frequency.name: {
                    const value = val / 1000000n;
                    const remainder = Number(val - value * 1000000n) / 1000000;
                    const result = Number(value) + remainder;
                    this.controllers.frequency.value = result;
                    const text = this.tabs.frequency.textContent;
                    const split = text.split(' : ');
                    this.tabs.frequency.textContent = split[0] + ' : ' + result;
                    break;
                }

                case ps.spectrumInversion.name:
                case ps.powerToLnb?.name:
                case ps.referenceToLnb?.name:
                case ps.mute.name: {

                    const c = this.controllers[name];
                    if (!c) {
                        console.log(`TThe ${name} controller is not ready yet.`);
                        break;
                    }

                    if (val === undefined) {
                        delete this._toRead[name];
                        c.disabled = true;
                        break;
                    }
                    c.checked = val;
                    break;
                }

                case ps.loSet?.name: {
                    this.controllers.loSet.value = val;
                    break;
                }

                case ps.lo?.name: {

                    const c = this.controllers.loSet;


                    delete this._toRead[name];

                    const length = Number(value?.length);
                    if (!length) {
                        c.disabled = true;
                        break;
                    }
                    c.show();
                    c.fill(value);
                    if (length === 1) {
                        c.disabled = true;
                        delete this._toRead[ps.loSet.name];
                    }
                    break;
                }

                case ps.alcOnOff?.name: {
                    this.controllers.alcLevel.anable = value;
                    break;
                }

                case ps.refCapability.name: {

                    const c = this.controllers.refSource;

                    delete this._toRead[name];

                    const length = Number(val?.length);
                    if (!length) {
                        c.disabled = true;
                        break;
                    }
                    c.show();
                    c.fill(val);
                    if (length === 1) {
                        c.disabled = true;
                        delete this._toRead[ps.refSource.name];
                    }
                    break;
                }

                case ps.refSource.name: {
                    this.controllers.refSource.value = val;
                    break;
                }

                default:
                    console.warn('[TODO]', { pl, [name]: val });
                case ps.alcProtectionRange?.name:
                case ps.alcProtectionThreshold?.name:
                case ps.alcProtectionOnOff?.name:
                case ps.flags?.name: {
                    delete this._toRead[name];
                }

            }
        });
    }

    /**
     * @param {method} e
     */
    set change(e) {
        this.#onChangeEvents.push(e);
    }

    _frequencyRange(val) {
        this._min(val[0]);
        this._max(val[1]);
        this._step(0.000001);
    }

    _min(min) {
        this.controllers.frequency.min = min;
    }

    _max(max) {
        this.controllers.frequency.max = max;
    }

    _step(step) {
        this.controllers.frequency.step(step);
    }

    _tickMarks(set) {
        this.controllers.frequency.tickMarks(set);
    }

    #replaceReadAll(pls) {
        delete this._toRead.readAll;
        pls.forEach(pl => {
            const { code } = pl.parameter;
            const name = this.parametersClass.toName(code);
            this._toRead[name] = code;
        });
    }

    #onLoad() {
        // Get references to elements
        this.tabs.attenuation = this._card.querySelector('#attenuationTab');
        this.tabs.frequency = this._card.querySelector('#frequencyTab');
        this.tabs.gain = this._card.querySelector('#gainTab');
        this.tabs.gainOffset = this._card.querySelector('#gainOffsetTab');
        this.tabs.alcLevel = this._card.querySelector('#alcLevelTab');

        // Add click listeners to tabs
        Object.values(this.tabs).forEach(tab => {
            tab.addEventListener('click', e => this.#tabClick(e));
        });

        const onValueChange = this.#onValueChange.bind(this);

        this.controllers.attenuation = this.#createController('attenuation', onValueChange);
        this.controllers.gain = this.#createController('gain', onValueChange);
        this.controllers.frequency = this.#createController('frequency', onValueChange);
        this.controllers.gainOffset = this.#createController('gainOffset', onValueChange);
        this.controllers.alcLevel = this.#createController('alcLevel', onValueChange);

        const chkBoxMute = this._card.querySelector('#btnMute');
        if (chkBoxMute)
            this.controllers.mute = this.#createToggleController(
                chkBoxMute, {
                statusOn: translator.translate('MUTED'),
                ststusOff: translator.translate('UNMUTED'),
                hoverOn: translator.translate('Unmute'),
                hoverOff: translator.translate('Mute'),
                cssOn: translator.translate('btn-warning'),
                ctrlKeyOn: translator.translate('Resend Mute'),
                ctrlKeyOff: translator.translate('Resend Unmute')
            });

        const chkBoxRef = this._card.querySelector('#btnRefToLnb');
        if (chkBoxRef)
            this.controllers.referenceToLnb = this.#createToggleController(
                chkBoxRef, {
                statusOn: 'Ref.is ON',
                ststusOff: 'Ref.is OFF',
                hoverOn: 'Set Off',
                hoverOff: 'Set ON'
            });

        const chkBoxPower = this._card.querySelector('#btnPowerToLnb');
        if (chkBoxPower)
            this.controllers.powerToLnb = this.#createToggleController(
                chkBoxPower, {
                statusOn: 'Power is ON',
                ststusOff: 'Poweris OFF',
                hoverOn: 'Set Off',
                hoverOff: 'Set ON'
            });

        const chkBoxInversion = this._card.querySelector('#btnSpecInversion');
        if (chkBoxInversion)
            this.controllers.spectrumInversion = this.#createToggleController(
                chkBoxInversion, {
                statusOn: 'Inverted',
                ststusOff: 'Non-inverted',
                hoverOn: 'Set Non-inverted',
                hoverOff: 'Set Inverted'
            });

        this.controllers.refSource = new SelectHelper(this._card.querySelector('#refSource'), toSend => this.#onChangeBtn(toSend));
        this.controllers.loSet = new SelectHelper(this._card.querySelector('#loSelect'), toSend => this.#onChangeBtn(toSend));

        //        this.elements.loSelect = this._card.querySelector('#loSelect');
        //        if (this.elements.loSelect) {
        //            this.elements.loSelect.addEventListener('change', this.#onChangeLoSelect.bind(this));
        //        }

        // Restore tab from cookie
        const tabCookies = Cookies.get('tabCookies');
        if (tabCookies) {
            const tabElement = document.getElementById(tabCookies);
            if (tabElement) {
                new bootstrap.Tab(tabElement).show();
            }
        } else if (this.tabs.attenuation) {
            new bootstrap.Tab(this.tabs.attenuation).show();
        }
        this._onLoad?.();
    }

    #createToggleController(
        chkBox, {
            statusOn,
            ststusOff,
            hoverOn,
            hoverOff,
            cssOn,
            ctrlKeyOn,
            ctrlKeyOff
        }) {

        const options = {
            status: {
                on: statusOn,
                off: ststusOff
            },
            hover: {
                on: hoverOn,
                off: hoverOff
            },
            css: {
                on: cssOn
            },
            ctrlKey: {
                on: ctrlKeyOn,
                off: ctrlKeyOff
            }
        }
        return new CheckboxHelper(chkBox, toSet => this.#onChangeBtn(toSet), options);
    }

    #createController(cssClass, onValueChange) {
        const div = this._card.querySelector(`div.${cssClass}`);
        div.dataset.controller = cssClass;
        const controller = new ControllerValue(div);
        controller.change = onValueChange;
        return controller;
    }

    #tabClick({ currentTarget: { id } }) {

        Object.values(this.controllers).forEach(c => c.hideTooltip?.());
        const name = id.replace('Tab', '');
        const controller = this.controllers[name];
        if (!controller)
            console.error(name);
        controller.active();
        Cookies.set('tabCookies', id, { expires: 365, path: '/' });
    }

    #onValueChange(object) {
        const cs = this.controllers;
        Object.entries(object).forEach(([key, value]) => {
            let toSend;
            let pId;
            let parameterCode;

            switch (key) {
                case cs.alcLevel.name:
                    if (value.enable != null) {
                        toSend = value.enable ? 1 : 0;
                        const alcOnOff = this.parametersClass.parameters.alcOnOff;
                        parameterCode = alcOnOff.code;
                        pId = packetId.alcOnOff;
                        break;
                    }
                case cs.gainOffset.name:
                case cs.gain.name:
                case cs.attenuation.name:
                    const packetIdName = key + 'Set'
                    pId = packetId[packetIdName];
                    if (!pId) {
                        console.warn('Have to add Packet ID: ' + packetIdName);
                        return;
                    }
                    parameterCode = this.parametersClass.parameters[key].code;
                    toSend = value * 10;
                    break;

                case cs.frequency.name:
                    pId = packetId.frequencySet;
                    parameterCode = this.parametersClass.parameters.frequency.code;
                    const floor = Math.floor(value);
                    const remainder = Math.round(value % 1 * 1000000);
                    toSend = BigInt(floor) * 1000000n + BigInt(remainder);
                    break;

                default:
                    console.log('To add key = ' + key);
                    return;
            }

            this._sendChange(pId, toSend, parameterCode);
        });
    }

    #parameterMap = {
        // deprecated
        btnMute: () => ({
            id: packetId.muteSet,
            parameter: this.parametersClass.parameters.mute.code,
            toSend: ({ value, ctrlKey }) => ctrlKey ? +!value : +value
        }),
        btnPowerToLnb: () => ({
            id: 'powerToLnbSet', // The ID is a string because if I forgot to add this ID to the packetId, there would be a missing ID message.
            parameter: this.parametersClass.parameters.powerToLnb.code,
            toSend: ({ value }) => value ? 2 : 3
        }),
        btnRefToLnb: () => ({
            id: 'refToLnbSet',
            parameter: this.parametersClass.parameters.referenceToLnb.code,
            toSend: ({ value }) => value ? 1 : 2
        }),
        btnSpecInversion: () => ({
            id: 'specInversionSet',
            parameter: this.parametersClass.parameters.spectrumInversion.code,
            toSend: ({ value }) => value ? 1 : 2
        }),
        loSelect: () => ({
            id: packetId.loSet,
            parameter: this.parametersClass.parameters.loSet.code,
            toSend: ({ value }) => value
        }),
        // new style code
        refSource: () => ({
            id: infoId.refSourceSet,
            codes: [this.parametersClass.parameters.refSource],
            toSend: ({ value }) => ({
                [this.parametersClass.parameters.refSource.code]: [Number(value)]
            })
        })
    };

    #onChangeBtn(toSet) {
        const map = this.#parameterMap[toSet.id]?.();

        if (!map) {
            console.warn(`Unknown toggle: ${toSet.id}`);
            return;
        }

        // new style code
        if (map.parameter == null) {
            const command = {
                type: infoType.command,
                packetId: map.id,
                groupId: this.groupId,
                data: {
                    values: map.toSend(toSet),
                    codes: map.codes
                },
                update: true
            };
            this._sendChange(command);
            return;
        }
        // deprecated
        const id = map.id;
        const toSend = map.toSend(toSet);
        const parameter = map.parameter;

        this._sendChange(id, toSend, parameter);
    }

    _sendChange() {
        this.#onChangeEvents.forEach(cb => cb(...arguments));
    }

    destroy() {
        Object.values(this.controllers).forEach(c => c.destroy?.());

        this.controllers = {};
        this.tabs = {};
        this.elements = {};
    }
}