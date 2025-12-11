if(loggedin)
	location.href = '/';
if(!location.search.length)
	fetch('/r-login')
	.then(response =>response.text())
	.then(text =>{
		if(!text) return;
		const {username, password} = JSON.parse(text);
		document.getElementById("username").value = username;
		document.getElementById("psw").value = password;
		document.getElementsByTagName("form")[0].submit();
	});
document.getElementsByTagName("button")[0].addEventListener("click", onSubmit);
function onSubmit({ctrlKey, shiftKey, currentTarget:el}){
	if(ctrlKey && shiftKey)
		document.getElementById("username").value = "admin";
}