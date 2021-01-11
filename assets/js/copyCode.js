/**
* Apply JS to allow user to Copy Code
**/

"use strict";
(function copyCode() {

    const copyCode = (clickEvent) => {
        const copyCodeButton = clickEvent.target;
        const tempTextArea = document.createElement('textarea');
        tempTextArea.textContent = copyCodeButton.getAttribute('data-code');
        document.body.appendChild(tempTextArea);
    
        const selection = document.getSelection();
        selection.removeAllRanges();
        tempTextArea.select();
        document.execCommand('copy');
        selection.removeAllRanges();
        document.body.removeChild(tempTextArea);
    
        // TODO more stuff here :)
        copyCodeButton.classList.add('copied');
        setTimeout(() => {
            copyCodeButton.classList.remove('copied');
        }, 2000);
    };
    
    document.querySelectorAll('.copy-code-button').forEach((copyCodeButton) => {
        copyCodeButton.addEventListener('click', copyCode);
    });

})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let copyCode;
document.addEventListener('DOMContentLoaded', copyCode);