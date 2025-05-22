
const $toastContainer = $('#toast-container');

function showToast(title, message, headerClass){

	let $toast = $('<div>', {class: 'toast', role: 'alert', 'aria-live': 'assertive', 'aria-atomic': true});
		$toast.append(
			$('<div>', {class: 'toast-header'})
			.append(
				$('<strong>', {class: 'me-auto', text: title})
			)
			.append(
				$('<button>', {class: 'btn-close', type: 'button', 'data-bs-dismiss': 'toast', 'aria-label': 'Close'})
			)
		)
		.append(
			$('<div>', {class: 'toast-body', text: message})
		)
	.appendTo($toastContainer)
	.on('hide.bs.toast', function(){this.remove();});

	if(headerClass)
		$toast.find('.toast-header').addClass(headerClass);

	new bootstrap.Toast($toast).show();
}

function postObject(url, object){
	var json = JSON.stringify(object);

	return $.ajax({
		url: url,
		type: 'POST',
		contentType: "application/json",
		data: json,
	    dataType: 'json'
	});
}
function blink($el, bottstrapClass){
	if(!bottstrapClass)
		bottstrapClass = 'connection-ok';
	$el.addClass(bottstrapClass);
	setTimeout(()=>$el.removeClass(bottstrapClass), 500);
}
