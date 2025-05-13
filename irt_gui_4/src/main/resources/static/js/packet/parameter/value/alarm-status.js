import {parseToInt} from '../../service/converter.js'
const severities 		= Object.freeze(['NO_ALARM'			, 'INFO'			, 'WARNING'			, 'MINOR'			, 'MAJOR'			, 'CRITICAL']);
const text 				= Object.freeze(['No Alarm'			, 'No Alarm'		, 'Warning'			, 'Warning'			, 'Alarm'			, 'Alarm']);
export const boorstrapClass 	= Object.freeze(['text-bg-success'	, 'text-bg-success'	, 'text-bg-warning'	, 'text-bg-warning'	, 'text-bg-danger'	, 'text-bg-danger']);

class AlarmStatus{
	index;
	severities;
	text;
	boorstrapClass;
}

export function status(bytes){
	const status = new AlarmStatus();

	status.id = parseToInt(bytes.splice(0,2));
	const index = parseToInt(bytes)&7;
	status.index = index;
	status.severities = severities[index];
	status.text = text[index];
	status.boorstrapClass = boorstrapClass[index];
	return status;
}
