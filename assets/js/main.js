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

    $('.navbar-burger').on('click', function(e) {
        e.preventDefault();
        $('.navbar-burger').toggleClass('is-active');
        $('.navbar-menu').toggleClass('is-active');
    });

   $('#show a').on('click', function(e) {
        e.preventDefault();
        $('#show a').text($('#show a').text() == 'More' ? 'Less' : 'More');
        $('.more').toggleClass('is-hidden');
        return false;
    });
       
});
