$(function() {
    var MKTO_FORM_ID = 3977;
    var theForm;

    window.MktoForms2.loadForm("//go.confluent.io", "582-QHX-262", MKTO_FORM_ID, function(form) {
        theForm = form;

        form.onSuccess(function(vals, thanksURL) {
            var $mktoForm = $('#mkto-form .form');

            $mktoForm.children(':not(.is-hidden)').hide();
            $mktoForm.children('.is-hidden').removeClass('is-hidden');

            return false;
        });
    });

    $('#mkto-form a').on('click', function() {
        $(this).css('z-index', 0).siblings('form').addClass('active').find('input').focus();
        $('#signup-notice').removeClass('is-hidden');

        return false;
    });

    $('#mkto-form form').on('submit', function() {
        var vals = {};

        // Update the real marketo form
        $(this).serializeArray().forEach(function(field) {
            vals[field.name] = field.value;
        });

        theForm.setValues(vals);

        // Submit marketo form
        theForm.submit();

        return false;
    });

    $('#show a').on('click', function(e) {
        e.preventDefault();
        $('#show a').text($('#show a').text() == 'More' ? 'Less' : 'More');
        $('.more').toggleClass('is-hidden');
        return false;
    }); 

    $('#show-101 a').on('click', function(e) {
        e.preventDefault();
        $('#show-101 a').text($('#show-101 a').text() == 'More' ? 'Less' : 'More');
        $('.more-101').toggleClass('is-hidden');
        return false;
    });

    // Toggles mobile menu open/closed
    $('.mobile-menu').on('click', function(e){
      $('.mobile-menu, .nav-mobile').toggleClass('open');
    });

    // Toggles accordion in mobile menu open/closed
    $('.accordion-item-wrapper').on('click', function() {
      $(this).toggleClass('collapsed');
      $(this).find('.arrow').toggleClass('down').toggleClass('up');
    });
});
