import {start as networkStart, stop as networkStop} from './panel-network.js'
import {start as alarmsStart, stop as alarmsStop} from './panel-alarms.js'
import {start as redundancyStart, stop as redundancyStop} from './panel-redundancy.js'

const $body = $('.userPanels');
const $tabs = $body.find('.nav-link').click(userTabsOnShow);

const userTabsCookies = Cookies.get('userTabsCookies');
if(userTabsCookies)
	new bootstrap.Tab($(`#${userTabsCookies}`)).show();
else
	new bootstrap.Tab($('#userTabAlarn')).show();

export function start(){

	const $selectedTab = $tabs.filter((_,el)=>el.classList.contains('active'));

	if($selectedTab.length)
		userTabsOnShow($selectedTab.prop('id'));
	else
		console.warn('User Tab is not selected.')
}

export function stop(){
	networkStop(); alarmsStop(); redundancyStop();
}
function userTabsOnShow(selected){

	stop();

	if(selected.currentTarget){
		Cookies.set('userTabsCookies', selected.currentTarget.id);
		selected = selected.currentTarget.id;
	}

	switch(selected){

		case 'userTabNetwork':
			networkStart();
			break;

		case 'userTabAlarn':
			alarmsStart();
			break;

		case 'userTabRedundancy':
			redundancyStart();
			break;

		default:
			console.warn('Have to ccreate ' + e.currentTarget.id);
	}
}