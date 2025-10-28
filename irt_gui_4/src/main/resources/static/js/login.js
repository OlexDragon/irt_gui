if(!location.search.length)
fetch('/r-login')
.then(response =>response.text())
.then(text =>{
	if (!text)return;
	const {username, password} = JSON.parse(text);
	document.getElementById("username").value = username;
	document.getElementById("psw").value = password;
	document.getElementsByTagName("form")[0].submit();
}); 