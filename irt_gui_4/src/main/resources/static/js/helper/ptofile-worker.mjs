// ptofile-worker.mjs

export default class ProfileWorker {

    #data;
    #promise;

    constructor(serialNumber) {
        if (!serialNumber)
            throw { warn: 'No serial number' };

        this.#data = {
            serialNumber,
            profilePath: undefined
        };
    }

    async getProfile() {
        if (this.#data.profilePath)
            return this.#data;

        if (this.#promise)
            return await this.#promise;

        this.#promise = $.get('/file/path/profile', {
            sn: this.#data.serialNumber
        });

        try {
            const path = await this.#promise;

            this.#data.profilePath = path;

            return this.#data;
        } catch {
            throw { warn: 'Unable to find profile' };
        } finally {
            this.#promise = undefined;
        }
    }
}