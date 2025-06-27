import * as serialPort from './serial-port.js'
import {start as networkStart, stop as networkStop, disable as networkDisable} from './panel-network.js'
import {start as alarmsStart, stop as alarmsStop} from './panel-alarms.js'
import {start as redundancyStart, stop as redundancyStop, disable as redundancyDisable} from './panel-redundancy.js'
import {start as comStart, stop as comStop, disable as comDisable} from './panel-com.js'
import {onStatusChange} from './panel-summary-alarm.js'

const $body = $('.userPanels');
const $tabs = $body.find('.nav-link').click(userTabsOnShow);

(()=>{
	const userTabsCookies = Cookies.get('userTabsCookies');
	if(userTabsCookies)
		new bootstrap.Tab($(`#${userTabsCookies}`)).show();
	else
		new bootstrap.Tab($('#userTabAlarm')).show();})();

serialPort.onStart(onStart);

function onStart(doRun){
	if(!doRun){
		stop();
	}
}

let run;
export function start(){

	run  = true;
	const $selectedTab = $tabs.filter((_,el)=>el.classList.contains('active'));

	if($selectedTab.length)
		userTabsOnShow($selectedTab.prop('id'));
	else
		console.warn('User Tab is not selected.')
}

export function stop(){
	run  = false;
	networkStop(); alarmsStop(); redundancyStop(); comStop();
}

export function disable(){
	networkDisable(); redundancyDisable(); comDisable();
}

function userTabsOnShow(selected){
	networkStop(); alarmsStop(); redundancyStop(); comStop();

	if(!run)
		return;

	if(selected.currentTarget){
		Cookies.set('userTabsCookies', selected.currentTarget.id);
		selected = selected.currentTarget.id;
	}

	switch(selected){

		case 'userTabNetwork':
			networkStart();
			break;

		case 'userTabAlarm':
			alarmsStart();
			break;

		case 'userTabRedundancy':
			redundancyStart();
			break;

		case 'userTabCOM':
			comStart();
			break;

		default:
			console.warn('Have to ccreate ' + selected);
	}
}
onStatusChange(alarmStatus =>{
	switch(alarmStatus.severities){
	case 'CRITICAL':
		$tabs.filter((_,el)=>el.id==='userTabAlarm').filter((_,el)=>!el.classList.contains('active')).click();
	}
});