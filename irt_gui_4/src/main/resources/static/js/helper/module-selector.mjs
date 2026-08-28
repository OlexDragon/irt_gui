// 

export default class ModuleSelector {

    #element;
    #modules = new Map();
    #changeEvents = new Set();

    constructor(element) {
        this.#element = element;
    }

    onChange(callback) {
        this.#changeEvents.add(callback);

        return () => this.#changeEvents.delete(callback);
    }

    setModules(modules) {
        this.clear();

        Object.entries(modules)
            .sort((a, b) => a[1] - b[1])
            .forEach(([name, value]) => {
                this.#add(name, value);
            });
    }

    setSelected(value) {
        const button = this.#modules.get(value);

        if (!button)
            return false;

        button.checked = true;

        return true;
    }

    getSelected() {
        for (const [value, button] of this.#modules) {
            if (button.checked)
                return value;
        }

        return undefined;
    }

	modules(){
		return this.#modules;
	}

    disable(disabled) {
        this.#modules.forEach(button => {
            button.disabled = disabled;
        });
    }

    clear() {
        this.#modules.clear();
        this.#element.replaceChildren();
    }

    #add(name, value) {
        const id = `module${value}`;

        const column = document.createElement('div');
        column.className = 'col';

        const button = document.createElement('input');
        button.type = 'radio';
        button.className = 'btn-check';
        button.name = 'moduleConnect';
        button.id = id;
        button.autocomplete = 'off';
        button.value = value;
        button.addEventListener(
            'change',
            this.#onSelectionChange
        );

        const label = document.createElement('label');
        label.htmlFor = id;
        label.className = 'btn btn-outline-primary form-control';
        label.textContent = name;

        column.append(button, label);

        this.#modules.set(value, button);
        this.#element.append(column);
    }

    #onSelectionChange = ({ currentTarget }) => {
        const value = +currentTarget.value;

        this.disable(true);

        this.#changeEvents.forEach(callback => {
            callback(value);
        });
    };
}