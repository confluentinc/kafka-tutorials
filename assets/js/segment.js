window.addEventListener('DOMContentLoaded', function () {
  // If target element is code copy
  function isTargetCodeCopyButton(elem) {
    return elem.classList.contains('fa-copy');
  }

  // Determine whether we should track or not
  function shouldTrack(e) {
    if (e.target.tagName === 'A' || isTargetCodeCopyButton(e.target)) {
      return { target: e.target };
    } else if (e.target.parentNode.tagName === 'A') {
      // For image/icon
      return { target: e.target, parent: e.target.parentNode };
    }

    // Not tracking this
    return null;
  }

  // Get the payload to be sent
  function getPayload({ target, parent }) {
    let hrefUrl;
    let text;

    if (parent) {
      hrefUrl = parent.href;

      if (target.classList.contains('logo')) {
        // Logo click
        text = 'logo';
      } else {
        text = target.getAttribute('title') || target.getAttribute('alt');
      }
    } else if (isTargetCodeCopyButton(target)) {
      // Code copy
      // @TODO: No spec yet
      return;
    } else {
      hrefUrl = target.href;
      text = target.textContent;
    }

    return {
      hrefUrl,
      text: (text || '').trim(),
    };
  }

  document.body.addEventListener('click', function (e) {
    const path = e.path || (e.composedPath && e.composedPath());
    const elems = shouldTrack(e);

    if (elems && path) {
      const payload = getPayload(elems);

      if (!payload) {
        // Do nothing
        return;
      }

      // Traverse the locations
      const location = path.reduce((location, node) => {
        // Using "data-tracking-location"
        if (!node?.dataset?.trackingLocation) {
          return location;
        } else if (location) {
          // Concat
          return `${node.dataset.trackingLocation} > ${location}`;
        }

        // First encounter
        return node.dataset.trackingLocation;
      }, '');

      // Start tracking logic
      analytics.track('Click', { ...payload, location });
    }
  });
});
