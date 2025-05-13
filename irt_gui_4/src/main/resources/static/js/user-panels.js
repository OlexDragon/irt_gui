import {start as networkStart, stop as networkStop} from './panel-network.js'
import {start as alarmsStart, stop as alarmskStop} from './panel-alarms.js'

const $body = $('.userPanels');
const $tabs = $body.find('.nav-link').click(userTabsOnShow);

const userTabsCookies = Cookies.get('userTabsCookies');
if(userTabsCookies)
	new bootstrap.Tab($(`#${userTabsCookies}`)).show();
else
	new bootstrap.Tab($('#userTabAlarn')).show();
	
function userTabsOnClick(e){
	Cookies.set('userTabsCookies', e.currentTarget.id);
}

export function start(){
	const $selectedTab = $tabs.filter((_,el)=>el.classList.contains('active'));
	if($selectedTab.length)
		userTabsOnShow($selectedTab.prop('id'));
	else
	console.warn('User Tab is not selected.')
}

export function stop(){
	networkStop(); alarmskStop();
}
function userTabsOnShow(e){

	stop();

	const selected = typeof e === 'string' ? e : e.currentTarget.id
	switch(selected){

		case 'userTabNetwork':
			networkStart();
			break;

		case 'userTabAlarn':
			alarmsStart();
			break;

		default:
			console.warn('Have to ccreate ' + e.currentTarget.id);
	}
}