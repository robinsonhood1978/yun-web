$.metadata.setType("attr", "vld");

$.validator.AlertError = {
	invalidHandler : function(form, validator) {
		var errors = validator.numberOfInvalids();
		if (errors) {
			for (var name in validator.invalid) {
				alert(validator.invalid[name]);
				return;
			}
		}
	},
	showErrors : function(errors) {
	}
};
$.validator.addMethod("username", function(value) {
	if(value.length==0) {return true;}
	var p = /^[0-9a-zA-Z\u4e00-\u9fa5\.\-@_]+$/;
	return p.exec(value) ? true : false;
}, "Please enter only letters,digits,chinese and '_','-','@'");
$.validator.addMethod("path", function(value) {
	if(value.length==0) {return true;}
	var p = /^[0-9a-zA-Z]+$/;
	return p.exec(value) ? true : false;
}, "Please enter only letters and digits");

$.validator.addMethod("isMobile", function(value, element) { 
	  var length = value.length; 
	  var mobile = /^(((13[0-9]{1})|(15[0-9]{1})|(18[0-9]{1}))+\d{8})$/; 
	  return this.optional(element) || (length == 11 && mobile.test(value)); 
}, "请正确填写您的手机号码"); 
//电话号码验证 
$.validator.addMethod("isTel", function(value, element) { 
var tel = /^\d{3,4}-?\d{7,9}$/; //电话号码格式010-12345678 
return this.optional(element) || (tel.test(value)); 
}, "请正确填写您的电话号码"); 
//邮政编码验证 
$.validator.addMethod("isZipCode", function(value, element) { 
var tel = /^[0-9]{6}$/; 
return this.optional(element) || (tel.test(value)); 
}, "请正确填写您的邮政编码"); 

//传真号码验证 
$.validator.addMethod("isFax", function(value, element) { 
var fax = /^\d{3,4}-?\d{7,9}$/; //传真号码格式010-12345678 
return this.optional(element) || (fax.test(value)); 
}, "请正确填写您的传真号码");

$.extend($.validator.messages, {
	required : "该项为必填项",
	remote : "请修正该字段",
	email : "请输入正确格式的电子邮件",
	url : "请输入合法的网址",
	date : "请输入合法的日期",
	dateISO : "请输入合法的日期 ",
	number : "请输入合法的数字",
	digits : "只能输入整数",
	creditcard : "请输入合法的信用卡号",
	equalTo : "请再次输入相同的值",
	accept : "请输入拥有合法后缀名的字符串",
	maxlength : $.format("请输入一个长度最多是 {0} 的字符串"),
	minlength : $.format("请输入一个长度最少是 {0} 的字符串"),
	rangelength : $.format("请输入一个长度介于 {0} 和 {1} 之间的字符串"),
	range : $.format("请输入一个介于 {0} 和 {1} 之间的值"),
	max : $.format("该项不能大于 {0}"),
	min : $.format("该项不能小于 {0}"),
	username : "只能输入字符、数字、中文、和 _ - @ 的组合",
	path : "只能输入字符和数字的组合"
});

$.fn.extend( {
	showBy : function(target) {
		var offset = target.offset();
		var top, left;
		var b = $(window).height() + $(document).scrollTop() - offset.top
				- target.outerHeight();
		var t = offset.top - $(document).scrollTop();
		var r = $(window).width() + $(document).scrollLeft() - offset.left;
		var l = offset.left + target.outerWidth() - $(document).scrollLeft();
		if (b - this.outerHeight() < 0 && t > b) {
			top = offset.top - this.outerHeight() - 1;
		} else {
			top = offset.top + target.outerHeight() + 1;
		}
		if (r - this.outerWidth() < 0 && l > r) {
			left = offset.left + target.outerWidth() - this.outerWidth();
		} else {
			left = offset.left;
		}
		this.css("top", top).css("left", left).show();
	}
});
