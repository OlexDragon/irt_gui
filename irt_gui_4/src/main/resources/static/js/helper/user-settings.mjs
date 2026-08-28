export default class UserSettings {

    static get(key, defaultValue = null) {
        const value = localStorage.getItem(key);
        return value ?? defaultValue;
    }

    static set(key, value) {
        localStorage.setItem(key, value);
    }

	static deleteAll(){
		for (const key of Object.keys(localStorage)) {
		    if (key.startsWith('value-panel:'))
		        localStorage.removeItem(key);
		}
	}
}
