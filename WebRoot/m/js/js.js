function form_submit(){
	//jAlert('这是一个可自定义样式的警告对话框', '警告对话框');
	document.getElementById("login").submit();
}
function form_reset(){
	document.getElementById("login").reset();
}
function gotoPage(actionUrl){
	if($("#pageNo").val()==''){
		$("#pageMsg").show();
		return false;
	}
	else{
		window.location= actionUrl +  $("#pageNo").val();
	}
	
}