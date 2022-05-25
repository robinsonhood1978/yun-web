;(function($){
  'use strict';
      // preloader
      $(window).on('load', function() { // makes sure the whole site is loaded 
        $('#status').fadeOut(); // will first fade out the loading animation 
        $('#preloader').delay(150).fadeOut('slow'); // will fade out the white DIV that covers the website. 
        $('body').delay(150).css({'overflow':'visible'});
      });

      $('#carousel').carousel({
          interval: 3000
      });
      $('.carousel').carousel({
          interval: false 
      });

      $('select').niceSelect();

      // owl
      $('.loop').owlCarousel({
          items: 3,
          loop: true,
          margin: 6,
          responsive:{
              600:{
                  items:6
              }
          }
      });

      // active page menu when click
      var url = window.location.href;
      $(".nav li a").each(function() {
          // checks if its the same on the address bar
          if (url === (this.href)) {
              $(this).closest("li").addClass("active");
          }
      });
          
      // top contact active
      var con = $(".contact");
      con.hide().eq(0).show();
      $('.list li').on('click', function(){
          var value = "#" + $(this).data("value");
          con.fadeOut(100);
          $(value).delay(150).fadeIn(250);
      });

      
      // FORM @v 1.0.3
  var contactForm = $('#contact-us'), 
    quoteForm = $('#quote-request'), 
    subscribeForm = $('#subscribe-me');
  if (quoteForm.length > 0 || contactForm.length > 0 || subscribeForm.length > 0) {
    if( !$().validate || !$().ajaxSubmit ) {
      console.log('quoteForm: jQuery Form or Form Validate not Defined.');
      return true;
    }
    // Quote Form - home page
    if (quoteForm.length > 0) {
      var selectRec = quoteForm.find('select.required'), 
        qf_results = quoteForm.find('.form-results');
      quoteForm.validate({
        ignore: [],
        errorPlacement: function(error, elm) {
          if (elm.is('select.required')) { error.insertAfter(elm.next('.nice-select')); } else { error.insertAfter(elm); }
        },
        invalidHandler: function () { qf_results.slideUp(400); },
        submitHandler: function(form) {
          qf_results.slideUp(400);
          $(form).ajaxSubmit({
            target: qf_results, dataType: 'json',
            success: function(data) {
              var type = (data.result==='error') ? 'alert-danger' : 'alert-success';
              qf_results.removeClass( 'alert-danger alert-success' ).addClass( 'alert ' + type ).html(data.message).slideDown(400);
              if (data.result !== 'error') { $(form).clearForm(); }
            }
          });
        }
      });
      selectRec.on('change', function() { $(this).valid(); });
    }
    // Contact Form - contact page
    if (contactForm.length > 0) {
      var cf_results = contactForm.find('.form-results');
      contactForm.validate({
        invalidHandler: function () { cf_results.slideUp(400); },
        submitHandler: function(form) {
          cf_results.slideUp(400);
          $(form).ajaxSubmit({
            target: cf_results, dataType: 'json',
            success: function(data) {
              var type = (data.result==='error') ? 'alert-danger' : 'alert-success';
              cf_results.removeClass( 'alert-danger alert-success' ).addClass( 'alert ' + type ).html(data.message).slideDown(400);
              if (data.result !== 'error') { $(form).clearForm(); }
            }
          });
        }
      });
    }
    // Subsribe - footer form
    if (subscribeForm.length > 0) {
      var sf_results = subscribeForm.find('.form-results');
      subscribeForm.validate({
        invalidHandler: function () { sf_results.slideUp(400); },
        submitHandler: function(form) {
          sf_results.slideUp(400);
          $(form).ajaxSubmit({
            target: sf_results, dataType: 'json',
            success: function(data) {
              var type = (data.result==='error') ? 'alert-danger' : 'alert-success';
              sf_results.removeClass( 'alert-danger alert-success' ).addClass( 'alert ' + type ).html(data.message).slideDown(400);
              if (data.result !== 'error') { $(form).clearForm(); }
            }
          });
        }
      });
    }
  }
  
})(jQuery);
