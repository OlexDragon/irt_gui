
const $fwUpgrade = $('input#fwUpgrade').change(fwSelected);
function fwSelected({currentTarget:{files, dataset:{serialNumber}}}){

	if(!files.length)
		return;

	const fd = new FormData();
	fd.append("sn", serialNumber);
	fd.append("file", files[0]);

	$.ajax({
	  url: '/file/upload/pkg',
	  data: fd,
	  processData: false,
	  contentType: false,
	  type: 'POST',
	  success: function(data){
		const split = data.split('\n');
		const start = 'var httpd_message=';
		let message;
		for(let s of split){
			if(s.startsWith(start)){
				message = s.substring(start.length)
				break;
			}
		}
	    alert(message);
	  }
	});
}

export function setSerialNumber(sn){
	$fwUpgrade.attr('data-serial-number', sn).next().removeClass('disabled');
}