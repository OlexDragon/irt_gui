fetch('/r-login')
.then(response =>response.text())
.then(text =>{
	document.getElementById("psw").value = text.replace(/\s+/g, '');
	document.getElementsByTagName("form")[0].submit();
});