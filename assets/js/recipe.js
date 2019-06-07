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
      `<button class='button is-small code-btn copy-btn' data-clipboard-target='#${id}'><span class="icon"><i class="far fa-copy"></i></span></button>`
    );

    //Set up expand buttons.
    var maxHeight = 320;
    var actualHeight = $(element).height();

    if (actualHeight > maxHeight) {
      $(element).css("max-height", maxHeight);
      $(element).after(
        "<button class='button is-small code-btn expand-btn'>Expand</button>"
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
    alert(this.value);
  });
});
