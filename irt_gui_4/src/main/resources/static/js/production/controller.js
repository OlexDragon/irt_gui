export default class Controller{

	#name;

	get name(){
		return this.#name;
	}

	set name(n){
		if(this.#name)
			throw new Error('Changing the controller name is prohibited');
		this.#name = n;
	}
}