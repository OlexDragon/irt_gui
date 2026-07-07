export default class CachedData{
	#fetcher;
	#url;
	#timeout;
	#lifeCycle;
	#data;
	constructor(fetcher, url, lifeCycle, timeout = 5000){
		this.#fetcher = fetcher;
		this.#url = url;
		this.#lifeCycle = lifeCycle;
		this.#timeout = timeout;
	}
	fetch(){
		if(this.#data)
			return this.#data;
		
		return this.#fetcher(this.#url).always(()=>setTimeout(()=>this.#data = null, this.#lifeCycle));
	}
}