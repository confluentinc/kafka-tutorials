const SCROLL_CLASSES = [
  'header',
  'logo-container',
  'mobile-menu'
].join(',.');

$(window).on('scroll', function(){

  // If scrolling & no scrolled classes
  if (window.scrollY > 0 && !$(SCROLL_CLASSES).hasClass('scrolled')) {
    $(SCROLL_CLASSES).addClass('scrolled');

  // If at top of page & scrolled classes
  } else if(window.scrollY === 0 && $(SCROLL_CLASSES).hasClass('scrolled')) {
    $(SCROLL_CLASSES).removeClass('scrolled');
  }
});

// Toggles mobile menu open/closed
$('.mobile-menu').on('click', function(){
  $('.mobile-menu, .nav-mobile').toggleClass('open');
});

// Toggles accordion in mobile menu open/closed
$('.accordion-item-wrapper').on('click', function() {
  $(this).toggleClass('collapsed');
  $(this).find('.arrow').toggleClass('down').toggleClass('up');
});
