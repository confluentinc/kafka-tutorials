$(document).ready(function() {
  // Build the TOC
  $("#toc").toc({
    content: "div.recipe-markup",
    headings: "h1,h2,h3,h4"
  });

  // Code highlight
  hljs.initHighlightingOnLoad();

  var clipboard = new ClipboardJS(".copy-btn");

  $("pre").each(function(index, element) {
    //Set up copy buttons.
    var id = "snippet-" + index;

    $(element).wrap("<div class='snippet-wrapper'></div>");
    $(element).attr("id", id);
    $(element).after(
      `<span class="icon copy-btn" data-clipboard-target='#${id}'><i class="far fa-copy"></i></span>`
    );

    //Set up expand buttons.
    var maxHeight = 320;
    var actualHeight = $(element).height();

    if (actualHeight > maxHeight) {
      $(element).css("max-height", maxHeight);
      $(element).after(
        "<span class='icon expand-btn'><i class='fas fa-expand-arrows-alt'></i></span>"
      );
    }
  });

  $(".expand-btn").click(function() {
    $(this)
      .prev("pre")
      .css("max-height", "");
    $(this).remove();
  });

  $(".num").each(function() {
    var text = $(this).text();
    $(this).html(text.substring(0, text.length - 1));
  });

  $(".cflt-options select").on("change", function() {
    window.location.href = this.value;
  });
});
