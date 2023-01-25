

window.addEventListener('DOMContentLoaded', function () {
  function copy(node) {
    let copyText;

    if (navigator.clipboard) {
      // Modern browser
      copyText = node.textContent;
      navigator.clipboard.writeText(copyText);
    } else {
      const selection = getSelection();
      selection.removeAllRanges();

      const range = document.createRange();
      range.selectNodeContents(node);
      selection.addRange(range);
      copyText = range.toString();

      document.execCommand('copy');
      selection.removeAllRanges();
    }

    return copyText;
  }

  // If target element is code copy
  function isTargetCodeCopyButton(elem) {
    return elem.classList.contains('fa-copy');
  }

  // Determine whether we should track or not
  function shouldTrack(e) {
    if (e.target.tagName === 'A') {
      return { target: e.target };
    } else if (
      isTargetCodeCopyButton(e.target) ||
      e.target.parentNode.tagName === 'A'
    ) {
      // For image/icon
      return { target: e.target, parent: e.target.parentNode };
    }

    // Not tracking this
    return null;
  }

  function identify (){
    if (!window?.reveal) {
      return;
    }
    
    const payload = { clearbit_company: window.reveal }
    
    window.analytics.identify(payload);
  }

  // Get the payload to be sent
  function getPayload({ target, parent }) {
    let eventName = 'Click';
    let hrefUrl;
    let text;

    if (isTargetCodeCopyButton(target)) {
      // Code copy
      eventName = 'Copy Code';
      text = copy(document.querySelector(parent.dataset.clipboardTarget));
      hrefUrl = window.location.pathname;
    } else if (parent) {
      hrefUrl = parent.href;

      if (target.classList.contains('logo')) {
        // Logo click
        text = 'logo';
      } else {
        text = target.getAttribute('title') || target.getAttribute('alt');
      }
    } else {
      hrefUrl = target.href;
      text = target.textContent;
    }

    return {
      eventName,
      hrefUrl,
      text: (text || '').trim(),
    };
  }

  document.body.addEventListener('click', function (e) {
    const path = e.path || (e.composedPath && e.composedPath());
    const elems = shouldTrack(e);

    if (elems && path) {
      const { eventName, ...payload } = getPayload(elems);

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
      analytics.track(eventName, { ...payload, location });
    }
  });

  identify();
});
