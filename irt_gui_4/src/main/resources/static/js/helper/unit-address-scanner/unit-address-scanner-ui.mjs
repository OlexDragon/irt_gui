// unit-address-scanner-ui.mjs

import UnitAddressScanner from './unit-address-scanner.mjs';

export default class UnitAddressScannerUI {
    #scanner;
    #modal;
    #body;

	constructor(card, { onFinish } = {}) {
	    this.#scanner = new UnitAddressScanner(card, {
	        onSent: address => this.#onSent(address),
	        onFound: address => this.#onFound(address),
	        onFinish
	    });
	}

    start() {
        this.#createModal();
        this.#scanner.start();
    }

    stop() {
        this.#scanner.stop();
        this.#modal?.hide();
    }

    #onFound(address) {
        const el = document.getElementById(`colId-${address}`);

        if (!el)
            return;

        el.classList.remove('text-secondary');
        el.classList.add('btn', 'btn-success', 'btn-sm');
        el.addEventListener('click', () => {
            this.#select(address);
        }, { once: true });
    }

	#onSent(address) {
	    const col = document.createElement('div');
	    col.id = 'colId-' + address;
	    col.className = 'col text-secondary text-end';
	    col.textContent = address;
	    this.#body.appendChild(col);
	}

    #select(address) {
        this.#modal.hide();
        const input = document.getElementById('unitAddress');
        input.value = address;
		input.dispatchEvent(new Event('change'))
    }

    #createModal() {
        this.#body = document.createElement('div');
        this.#body.className = 'row';

        const modalDiv = document.createElement('div');
        modalDiv.className = 'modal fade';
        modalDiv.tabIndex = -1;

        const modalDialog = document.createElement('div');
        modalDialog.className = 'modal-dialog modal-dialog-centered';

        const modalContent = document.createElement('div');
        modalContent.className = 'modal-content';

        // Header
        const modalHeader = document.createElement('div');
        modalHeader.className = 'modal-header';
        const title = document.createElement('h5');
        title.className = 'modal-title';
        title.textContent = 'Unit Address Scan';
        modalHeader.appendChild(title);

        const closeBtn = document.createElement('button');
        closeBtn.type = 'button';
        closeBtn.className = 'btn-close';
        closeBtn.setAttribute('data-bs-dismiss', 'modal');
        closeBtn.setAttribute('aria-label', 'Close');
        modalHeader.appendChild(closeBtn);
        modalContent.appendChild(modalHeader);

        // Body
        const modalBody = document.createElement('div');
        modalBody.className = 'modal-body';
        modalBody.appendChild(this.#body);
        modalContent.appendChild(modalBody);

        modalDialog.appendChild(modalContent);
        modalDiv.appendChild(modalDialog);
        document.body.appendChild(modalDiv);

        this.#modal = new bootstrap.Modal(modalDiv);
        modalDiv.addEventListener('hidden.bs.modal', () => {
			this.#scanner.stop();
            modalDiv.remove();
        });
        this.#modal.show()
    }
}