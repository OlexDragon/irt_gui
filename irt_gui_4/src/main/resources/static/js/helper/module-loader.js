export default class ModuleLoader {
    #url;
    #script;
    #promise

    async load(url) {
        if (url.startsWith('./'))
            url = '.' + url;

        if (url !== this.#url) {
            this.#url = url;

            try {
                // catch synchronous evaluation errors
                const p = import(url);
                this.#promise = p.catch(error => {
                    console.error('Promise failed (async)', error);
                    throw error;
                });
            } catch (error) {
                console.error('Promise failed (sync)', error);
                throw error;
            }
        }

        return this.#promise;
    }
}
//new ScriptLoader().load('/controller/controller-irpc.js').then(c=>console.log(c));