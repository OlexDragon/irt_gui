export default class ModuleLoader{
	#url;
	#script;

	async load(url){
		url = '.' + url;
		if(url !== this.#url){
			this.#url = url;
			this.#script = await import(url);
		}
		return new Promise((resolve)=>resolve(this.#script));
	}
}

//new ScriptLoader().load('/controller/controller-irpc.js').then(c=>console.log(c));