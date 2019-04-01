$(document).ready(function() {
  $('#software-tab-choices .button').on('click', function() {
    var tab = $(this).data('tab');

    $('#software-tab-choices span').removeClass('is-selected');
    $(this).addClass('is-selected');

    $('#tab-content > .recipe-tab').removeClass('is-active');
    $('.recipe-tab[data-content="' + tab + '"]').addClass('is-active');
  });
});
