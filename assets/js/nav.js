// Toggles mobile menu open/closed
$('.mobile-menu').on('click', function(e){
  $('.mobile-menu, .nav-mobile').toggleClass('open');
});

// Toggles accordion in mobile menu open/closed
$('.accordion-item-wrapper').on('click', function() {
  $(this).toggleClass('collapsed');
  $(this).find('.arrow').toggleClass('down').toggleClass('up');
});