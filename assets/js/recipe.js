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
    //Set up copy buttons.
    var id = 'snippet-' + index;

    $(element).wrap("<div class='snippet-wrapper'></div>");
    $(element).attr('id', id);
    $(element).after(`<button class='button is-small code-btn copy-btn' data-clipboard-target='#${id}'><span class="icon"><i class="far fa-copy"></i></span></button>`);

    //Shrink snippets.
    var maxHeight = 320;
    var actualHeight = $(element).height();

    if (actualHeight > maxHeight) {
      $(element).css('max-height', maxHeight);
      $(element).after("<button class='button is-small code-btn expand-btn'>Expand</button>");
    }
  });

  $('.expand-btn').click(function() {
    $(this).prev('pre').css('max-height', '');
    $(this).remove();
  });
});
