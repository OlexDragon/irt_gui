// controller-toggle.mjs

export default class CheckboxHelper {

    #root;
    #checkBox;
    #label;
    #ctrlKey = false;

    constructor(chkBox, callback, options = {}) {
        this._options = this.#setDefaults(options);

        this.#checkBox = chkBox;
        this.#root = chkBox.parentElement;

        if (this._options.ctrlKey.on)
            this.#checkBox.addEventListener(
                'click',
                ({ ctrlKey }) => {
                    this.#ctrlKey = ctrlKey;
                }
            );

        this.#checkBox.addEventListener(
            'change',
            () => {

                this.#update();

                callback?.({
                    id: this.#checkBox.id,
                    value: this.checked,
                    state: this.#state,
                    ctrlKey: this.#ctrlKey
                });
            }
        );

        this.#label = this.#root.querySelector('label');
        const { content } = this._options.hover;
        if (content) {
            this.#label.addEventListener(
                'mouseenter',
                ({ ctrlKey }) => this.#onHover({
                    isHovering: true,
                    cfgKey: ctrlKey ? 'ctrlKey' : 'hover'
                }));
            this.#label.addEventListener(
                'mouseleave',
                () => this.#onHover({
                    isHovering: false,
                    cfgKey: 'hover'
                }));
        }
        this.#update();
    }

    #setDefaults(options) {
        return {
            status: {
                on: 'Enabled',
                off: 'Disabled',
                ...options.status
            },
            hover: {
                on: 'Click to disable',
                off: 'Click to enable',
                content: true,
                tooltip: false,
                ...options.hover
            },
            css: {
                on: 'btn-success',
                off: 'btn-outline-secondary',
                ...options.css
            },
            ctrlKey: {
                on: null,
                off: null,
                ...options.ctrlKey
            }
        };
    }

    #show = () => {
        this.#root.classList.remove('visually-hidden');
        this.#checkBox.disabled = false;
    };

    get checked() {
        return this.#checkBox.checked;
    }

    set checked(value) {
        if (typeof value !== 'boolean') {
            console.warn('variable value is not boolean.', { value });
            this.#checkBox.disabled = true;
            return;
        }

        this.#show?.();
        this.#show = null;

        if (value === this.#checkBox.checked)
            return;

        this.#checkBox.checked = value;
        this.#update();
    }

    get disabled() {
        return this.#checkBox.disabled;
    }

    /**
     * @param {boolean} disabled
     */
    set disabled(disabled) {
        this.#checkBox.disabled = disabled;
    }

    get #state() {
        return this.#checkBox.checked ? 'on' : 'off';
    }

    #update() {
        const state = this.#state;
        const cfg = this._options;

        this.#label.textContent = cfg.status[state];

        if (cfg.hover.tooltip)
            this.#label.title = cfg.hover[state];

        this.#label.classList.toggle(cfg.css.on, state === 'on');
        this.#label.classList.toggle(cfg.css.off, state === 'off');
    }

    #onHover({ isHovering, cfgKey }) {
        const state = this.#state;
        const cfg = this._options[cfgKey]
        const { status, hover } = this._options;

        this.#label.textContent = isHovering
            ? cfg[state] ?? hover[state]
            : status[state];

    }

}