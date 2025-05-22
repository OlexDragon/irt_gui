import Parameter from'./parameter.js'

export default class Payload{

		constructor(parameter, data){
		// From bytes
		if(Array.isArray(parameter)){
			this.parameter = new Parameter(parameter.subarray(0, PARAMETER_SIZE));
			if(this.parameter.size)
				this.data = parameter.subarray(PARAMETER_SIZE);
			return;
		}
		if(typeof parameter === 'number')
			this.parameter = new Parameter(parameter)
		else if(parameter)
			this.parameter = parameter;
		else
			this.parameter =  new Parameter();
		if(data){
			this.data = data;
			this.parameter.size = data.length;
		}
	}

	toBytes(){
		if(this.data)
			return this.parameter.toBytes().concat(this.data);
		return this.parameter.toBytes();
	}

	toString(packetGroupId){
		let str;
		if(!this.data)
			str = '';
		else if(packetGroupId){
			str = this.parameter.toString(packetGroupId);
			if(!str)
				str = this.data;
		}else
			str = this.data;
		return 'Payload:{Parameter:{' + this.parameter.toString(packetGroupId) + '}' + str + '}'
	}

	getData(packetGroupId){
//		if(!this.data)
//			return null;
//		else if(packetGroupId){
//			const tmp = parameterCode[packetGroupId][this.parameter.code];
//			if(tmp)
//				return tmp.parseFunction(this.data);
//			else
//				return this.data;
//		}else
			return this.data;
	}
}