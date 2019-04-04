$(document).ready(function() {
  $('#software-tab-choices .button').on('click', function() {
    var tab = $(this).data('tab');

    $('#software-tab-choices span').removeClass('is-selected');
    $(this).addClass('is-selected');

    $('#tab-content > .recipe-tab').removeClass('is-active');
    $('.recipe-tab[data-content="' + tab + '"]').addClass('is-active');
  });

  var clipboard = new ClipboardJS('.copy-btn');

  $('pre').each(function(index, element) {
    var id = 'snippet-' + index;

    $(element).wrap("<div class='snippet-wrapper'></div>");
    $(element).attr('id', id);
    $(element).after(`<button class='button is-small copy-btn' data-clipboard-target='#${id}'>Copy</button>`);
  });
});
