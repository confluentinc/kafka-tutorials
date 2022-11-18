const SCROLL_DEPTH_LEVELS = [20, 40, 60, 80, 99];

window.addEventListener('DOMContentLoaded', function () {
    let observer
    const el = document.getElementsByTagName('body')[0];
    
    el.style.position = 'relative';

    const initObserver = () => {
      const cb = (entries) => {
      const [IntersectionObserverEntry] = entries;
      const { isIntersecting, target } = IntersectionObserverEntry;
      const position = Number(target.dataset.depthMarkerId);

      if (isIntersecting) {
       analytics.track('Scroll Depth', {
          scrollDepth: position === 99 ? 100 : position // triggering at 99% is more consistent than 100%.
        });
    
        observer.unobserve(target);
      }
    };

     observer = new IntersectionObserver(cb);

      SCROLL_DEPTH_LEVELS.forEach((position) => {
        let markerEl = document.querySelector(
          `[data-depth-marker-id="${position}"]`
        );
  
        if (!markerEl) {
          markerEl = document.createElement('i');
          markerEl.style.position = 'absolute';
          markerEl.style.top = `${position}%`;
          markerEl.dataset.depthMarkerId = position;
  
          el.appendChild(markerEl);
        }
  
        observer.observe(markerEl);
      });

    }

    initObserver();
  });
  