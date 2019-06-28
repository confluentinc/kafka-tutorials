$(document).ready(function() {
  var CODE_BLOCK_HEIGHT = 320;

  // Build the TOC
  $("#toc").toc({
    content: "div.recipe-markup",
    headings: "h1,h2,h3,h4"
  });

  // Code highlight
  hljs.initHighlightingOnLoad();

  var clipboard = new ClipboardJS(".copy-btn");

  $('.recipe-try-it-step .subtitle .text').each(function() {
    // Do this so people can write recipe markup easier
    $(this).replaceWith(this.innerHTML.replace(/^(\d(\.)?)(.*)$/, '<div class="num">$1</div><div class="text">$3</div>'));
  });

  $(".num").each(function() {
    var $this = $(this);
    var text = $this.text();
    $this.html(text.substring(0, text.length - 1));
  });

  $("pre").each(function(index, element) {
    //Set up copy buttons.
    var id = "snippet-" + index;
    var $element = $(element);

    $element.wrap("<div class='snippet-wrapper'></div>");
    $element.attr("id", id);
    $element.after(
      `<span class="icon copy-btn" data-clipboard-target='#${id}'><i class="far fa-copy"></i></span>`
    );

    //Set up expand buttons.
    var actualHeight = $element.height();

    if (actualHeight > CODE_BLOCK_HEIGHT) {
      $element.css("max-height", CODE_BLOCK_HEIGHT);
      $element.after(
        "<span class='icon toggle-expand expand-btn'><i class='fas fa-expand-arrows-alt'></i></span><span class='icon toggle-expand compress-btn is-hidden'><i class='fas fa-compress-arrows-alt'></i></span>"
      );
    }
  });

  $('.expand-btn').click(function() {
      $(this)
        .siblings("pre")
        .css("max-height", "");

      this.classList.add('is-hidden');
      $('.compress-btn').removeClass('is-hidden');
  });

  $('.compress-btn').click(function() {
      $(this)
        .siblings("pre")
        .css("max-height", CODE_BLOCK_HEIGHT);

      this.classList.add('is-hidden');
      $('.expand-btn').removeClass('is-hidden');
  });

  $(".cflt-options select").on("change", function() {
    window.location.href = this.value;
  });
});
