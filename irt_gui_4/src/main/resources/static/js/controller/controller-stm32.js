import {getCommand} from '../flash/flash-command.js'
import FlashAnswer from '../flash/flash-answer.js';

export default class ControllerFlash{
	constructor(){
	}

// activate the UART bootloader and get the controller ID
	connect(requestPacket){
		requestPacket.commandName = 'CONNECT';
		requestPacket.bytes = getCommand(requestPacket.commandName);
		requestPacket.expectedLength = 1;
		send(requestPacket).done(data=>{
			if(!data?.answer){
				requestPacket.error = 'Connect Failed';
				requestPacket.f_parseData(data);
				return;
			}
			const answer = new FlashAnswer(data.answer[0]);
			switch(answer.name){

			case 'NACK':
				requestPacket.error = 'The answer is NACK';
				requestPacket.f_parseData(data);
				break;

			case 'ACK':
				this.getID(requestPacket);
				break;
	
			default:
				console.warn('Unknown Answer', data);
			}
		});
	}

// Get controller ID
	getID(requestPacket){
		requestPacket.commandName = 'GET_ID';
		requestPacket.bytes = getCommand(requestPacket.commandName);
		requestPacket.expectedLength = 5;
		send(requestPacket).done(requestPacket.f_parseData);
	}
}

function send(requestPacket){

	const json = JSON.stringify(requestPacket);

	return $.ajax({
		url: '/serial/send',
		type: 'POST',
		contentType: "application/json",
		data: json,
	    dataType: 'json'
	});
}