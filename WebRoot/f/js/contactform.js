jQuery(document).ready(function($) {
  "use strict";

  //Contact
  $('form.contactForm').submit(function() {
    var f = $(this).find('.form-group'),
      ferror = false,
      emailExp = /^[^\s()<>@,;:\/]+@\w[\w\.-]+\.[a-z]{2,}$/i;

    f.children('input').each(function() { // run all inputs

      var i = $(this); // current input
      var rule = i.attr('data-rule');

      if (rule !== undefined) {
        var ierror = false; // error flag for current input
        var pos = rule.indexOf(':', 0);
        if (pos >= 0) {
          var exp = rule.substr(pos + 1, rule.length);
          rule = rule.substr(0, pos);
        } else {
          rule = rule.substr(pos + 1, rule.length);
        }

        switch (rule) {
          case 'required':
            if (i.val() === '') {
              ferror = ierror = true;
            }
            break;

          case 'minlen':
            if (i.val().length < parseInt(exp)) {
              ferror = ierror = true;
            }
            break;

          case 'email':
            if (!emailExp.test(i.val())) {
              ferror = ierror = true;
            }
            break;

          case 'checked':
            if (! i.is(':checked')) {
              ferror = ierror = true;
            }
            break;

          case 'regexp':
            exp = new RegExp(exp);
            if (!exp.test(i.val())) {
              ferror = ierror = true;
            }
            break;
        }
        i.next('.validation').html((ierror ? (i.attr('data-msg') !== undefined ? i.attr('data-msg') : 'wrong Input') : '')).show('blind');
      }
    });
    f.children('textarea').each(function() { // run all inputs

      var i = $(this); // current input
      var rule = i.attr('data-rule');

      if (rule !== undefined) {
        var ierror = false; // error flag for current input
        var pos = rule.indexOf(':', 0);
        if (pos >= 0) {
          var exp = rule.substr(pos + 1, rule.length);
          rule = rule.substr(0, pos);
        } else {
          rule = rule.substr(pos + 1, rule.length);
        }

        switch (rule) {
          case 'required':
            if (i.val() === '') {
              ferror = ierror = true;
            }
            break;

          case 'minlen':
            if (i.val().length < parseInt(exp)) {
              ferror = ierror = true;
            }
            break;
        }
        i.next('.validation').html((ierror ? (i.attr('data-msg') != undefined ? i.attr('data-msg') : 'wrong Input') : '')).show('blind');
      }
    });
    if (ferror) return false;
    else var str = $(this).serialize();
    var action = $(this).attr('action');
    if( ! action ) {
      action = '/api/contact/';
    }
     var files = $('#msgFile').prop('files');
      console.log(files.length);
     var name = $('#name').val();
     var email = $('#email').val();
     var subject = $('#subject').val();
     var message = $('#message').val();
     
     console.log(email);
     console.log(message);
    
     
		var data = new FormData();
		data.append("name", name);
		data.append("email", email);
		data.append("subject", subject);
		data.append("message", message);
		for (var i = 0; i < files.length; i++) {
				 data.append('files'+i, files[i]);
			}
	  

	  var index = layer.load(1, {
    	shade: [0.5,'#000'] //0.1透明度的背景
	});
   $.ajax({
      type: "POST",
      url: action,
      data: data,
      cache: false,
      processData: false,
      contentType: false,
      success: function(data) {
        if (data.msg == 'OK') {
        	layer.close(index);
			document.contactForm.reset(); 
			layer.open({
				title:'Information',
				btn:'OK',
				content:'Success'
			});
        } else {
            layer.open({
				title:'Information',
				btn:'Fail',
				content:'Success'
			});
        }

      }
    });
    return false;
  });

});
