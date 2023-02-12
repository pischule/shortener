let copyTimeoutID = null;

const redirectInput = document.querySelector("#redirect-input");
const copyRedirectButton = document.querySelector("#copy-redirect-button");

function copyRedirectToClipboard() {
    redirectInput.select();
    document.execCommand("copy")
    copyRedirectButton.textContent = 'Copied';

    clearInterval(copyTimeoutID);
    copyTimeoutID = setTimeout(() => {
        copyRedirectButton.textContent = 'Copy';
    }, 1000);
}

copyRedirectButton?.addEventListener('click', copyRedirectToClipboard);