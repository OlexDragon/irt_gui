// profile-button.mjs

import ProfileWorker from './ptofile-worker.mjs';

export default class ProfileButton {

    #nav;
    #worker;
    #type;

    constructor(nav) {
        this.#nav = nav;
    }

    async setSerialNumber(serialNumber) {
        this.#remove();

        try {
            this.#worker = new ProfileWorker(serialNumber);

            const data = await this.#worker.getProfile();

            if (admin)
                this.#show(data);
        } catch (e) {
            console.warn('Unable to load profile:', e);
        }
    }

    setType(type) {
        this.#type = type;
        this.#changePath();
    }

    #show(data) {
        if (!data?.profilePath) {
            console.warn('No profile path found', data);
            return;
        }

        this.#remove();

        const fragment = document.createDocumentFragment();

        data.profilePath.forEach(path => {
            fragment.append(this.#createButton(path));
        });

        this.#nav.append(fragment);

        this.#changePath();
    }

    #createButton(path) {
        const encodedPath = encodeURIComponent(path);

        const group = document.createElement('div');
        group.className = 'col-auto btn-group ms-2 profile-button';

        const profile = document.createElement('a');
        profile.className = 'btn btn-outline-secondary';
        profile.href = `/file/open?p=${encodedPath}`;
        profile.textContent = 'Profile';
        profile.title = path;
        profile.addEventListener('click', this.#post);

        const toggle = document.createElement('button');
        toggle.type = 'button';
        toggle.className =
            'btn btn-outline-secondary dropdown-toggle dropdown-toggle-split';
        toggle.dataset.bsToggle = 'dropdown';
        toggle.ariaExpanded = 'false';

        const hidden = document.createElement('span');
        hidden.className = 'visually-hidden';
        hidden.textContent = 'Toggle Dropdown';

        toggle.append(hidden);

        const menu = document.createElement('ul');
        menu.className = 'dropdown-menu';

        menu.append(
            this.#createMenuItem(
                `/file/location?p=${encodedPath}`,
                'Location'
            ),
            this.#createUploadItem(encodedPath)
        );

        group.append(profile, toggle, menu);

        return group;
    }

    #createMenuItem(href, text) {
        const item = document.createElement('li');

        const link = document.createElement('a');
        link.className = 'dropdown-item';
        link.href = href;
        link.textContent = text;
        link.addEventListener('click', this.#post);

        item.append(link);

        return item;
    }

    #createUploadItem(encodedPath) {
        const item = document.createElement('li');

        const link = document.createElement('a');
        link.className = 'dropdown-item profileUpload';
        link.href = `/file/upload/profile?p=${encodedPath}`;
        link.textContent = 'Upload';
        link.addEventListener('click', event=>this.#upload(event));

        item.append(link);

        return item;
    }

    #changePath() {
        if (!this.#type)
            return;

        this.#nav
            .querySelectorAll('.profileUpload')
            .forEach(upload => {
                const search = upload.href.split('?')[1];

                if (
                    this.#type.name.startsWith('CONVERTER') ||
                    this.#type.name.startsWith('REFERENCE_BOARD')
                ) {
                    upload.href =
                        `/upgrade/rest/profile/${serialPort.serialPort}/0?${search}`; // M&C upload 
                } else {
                    upload.href =
                        `/file/upload/profile?${search}`;	// http upload
                }
            });
    }

    async #post(e) {
        e.preventDefault();

        const { href } = e.currentTarget;

        try {
            const response = await fetch(href, {
                method: 'POST'
            });

            if (!response.ok) {
                serialPort.showToast(
                    'File not found',
                    `The file\n${href}\nwas not found.`,
                    'text-bg-danger bg-opacity-50'
                );
            }
        } catch (error) {
            console.error(error);
        }
    }

    async #upload(e) {
        await this.#post(e);

        serialPort.showToast(
            'Profile Upgrade',
            'The update is starting, please wait for the profile to load.'
        );
    }

    #remove() {
        this.#nav
            .querySelectorAll('.profile-button')
            .forEach(el => el.remove());
    }
}