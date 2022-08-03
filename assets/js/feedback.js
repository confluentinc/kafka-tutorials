window.Userback = window.Userback || {};
Userback.access_token = '35466|70530|p6bqdPGbEaCYbpNutbeJVnQG4';
(function(d) {
    const s = d.createElement('script');
    s.async = true;
    s.src = 'https://static.userback.io/widget/v1.js';
    (d.head || d.body).appendChild(s);
})(document);

window.Userback.on_open = () => {
    // Timeout is required to actually update the elements
    setTimeout(() => {
        const emailInput = document.querySelector(
            '.userback-controls-form input[type=email]'
        );

        if (emailInput) {
            emailInput.placeholder = '(Optional) Email address';
        }

        const attachScreenshotTextUserbackDiv = document.querySelector(
            '.userback-controls-screenshot ubdiv'
        );

        if (attachScreenshotTextUserbackDiv) {
            attachScreenshotTextUserbackDiv.innerHTML =
                '(Optional) Attach a screenshot';
            attachScreenshotTextUserbackDiv.style.color = '#bac3cb'; // cannot set via css file
        }
    });
};
