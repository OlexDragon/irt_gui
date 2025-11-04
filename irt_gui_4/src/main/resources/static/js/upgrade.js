import * as serialPort from './serial-port.js';
import { type, onSerialChange } from './panel-info.js';

const $upgradeName = $('#upgradeName').on('change', checkWeb);
const $upgradeBody = $('#upgradeBody');
const $unitAddress = $('#unitAddress');
const $btnUpgradeGet = $('#btnUpgradeGet').on('click', checkBtnGet);

onSerialChange(serialNumberChange);
function serialNumberChange(serialNumber){

	if ($upgradeName.val() && !confirm(`Do you want to overwrite the entered name with "${serialNumber}"?`))
		return;

	$upgradeName.val(serialNumber).change();
}

function checkWeb({currentTarget:{value:upgradeName}}) {
	console.log('checkWeb', upgradeName);
	if(upgradeName){
		$upgradeName.prop('disabled', true);
		$.get('/upgrade/rest/list', {upgradeName: upgradeName}, data => {
			$btnUpgradeGet.prop('disabled', false);
			$upgradeName.prop('disabled', false);
			if(data?.length)
				$upgradeBody.empty().append(data.map(d=>$('<div>', {class: 'row'}).append($('<div>', {class: 'col', text: d})).append($('<div>', {class: 'col-auto'}).append($('<button>', {type: "button", value: encodeURIComponent(d), text: 'Upgrade', class: 'btn btn-outline-primary'}).click(btnClick)))));
			else{
				const val = $upgradeName.val();
				$upgradeBody.empty().text('No files found for "'+val+'"');
			}
		});
	}
}

function btnClick({currentTarget:{value}}) {

	if (!confirm('Are you sure you want to upgrade the unit with this file?'))
		return;

	const sp = serialPort.serialPort;
	const name = $upgradeName.val();
	const address = $unitAddress.val();
	const file = decodeURIComponent(value);
	
	$.post('/upgrade/rest', {sp, name, address, file}, data=>{
		alert(data);
	});
}
function checkBtnGet() {
	if($upgradeName.prop('disabled'))
		return;
	$btnUpgradeGet.prop('disabled', true);
	$upgradeName.change();
}