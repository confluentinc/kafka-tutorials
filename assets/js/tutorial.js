$(document).ready(function () {
  var CODE_BLOCK_HEIGHT = 320;

  // Code highlight
  hljs.initHighlightingOnLoad();

  var clipboard = new ClipboardJS('.copy-btn');

  clipboard.on('success', function (e) {
    e.clearSelection();
    e.trigger.classList.add('copied');

    setTimeout(function () {
      e.trigger.classList.remove('copied');
    }, 1500);
  });

  $('pre').each(function (index, element) {
    //Set up copy buttons.
    var id = 'snippet-' + index;
    var $element = $(element);

    $element.wrap("<div class='snippet-wrapper'></div>");
    $element.attr('id', id);
    $element.after(
      `<span class="icon copy-btn" data-clipboard-target='#${id}'><i class="fa fa-files-o"></i></span>`
    );

    //Set up expand buttons.
    var actualHeight = $element.height();

    if (actualHeight > CODE_BLOCK_HEIGHT) {
      $element.css('max-height', CODE_BLOCK_HEIGHT);
      $element.after(
        "<span class='icon toggle-expand expand-btn'><i class='fa fa-expand'></i></span><span class='icon toggle-expand compress-btn is-hidden'><i class='fa fa-compress'></i></span>"
      );
    }
  });

  $('.expand-btn').click(function () {
    $(this).siblings('pre').css('max-height', '');

    this.classList.add('is-hidden');
    $('.compress-btn').removeClass('is-hidden');
  });

  $('.compress-btn').click(function () {
    $(this).siblings('pre').css('max-height', CODE_BLOCK_HEIGHT);

    this.classList.add('is-hidden');
    $('.expand-btn').removeClass('is-hidden');
  });

  $('.cflt-options select').on('change', function () {
    window.location.href = this.value;
  });

  $('.navbar-burger').on('click', function (e) {
    e.preventDefault();
    $('.navbar-burger').toggleClass('is-active');
    $('.navbar-menu').toggleClass('is-active');
  });
});
