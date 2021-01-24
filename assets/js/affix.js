/**
* Affix the sidebars
*/

"use strict";
(function affix() {

    /**
    * Affix the sidebars
    */
    const primary = document.querySelector('.primary-holder');
    const left = document.getElementById('affixed-left-sidebar');
    const right = document.getElementById('affixed-right-sidebar');
    const footer = document.querySelector('body > .footerContainer');
    if (left === null && right === null) {
        return;
    }
    // update the position of the affixed content
    function update() {
        const spacing = 48;
        const leftspacing = 24;
        const rightspacing = 12;
        const viewportHeight = window.innerHeight;
        const top = Math.max(0, primary.getBoundingClientRect().top - 32) + spacing;
        const bottom = Math.max(0, viewportHeight - footer.getBoundingClientRect().top) + spacing;
        if (left !== null && !left.hasAttribute('disable-affix')) {
            left.style.width = `${getParentColumnWidth(left) - leftspacing * 2}px`;
            left.style.top = `${top}px`;
            left.style.bottom = `${bottom}px`;
        }
        if (right !== null) {
            right.style.width = `${Math.max(getParentColumnWidth(right) - rightspacing * 2, 136)}px`;
            right.style.top = `${top}px`;
            right.style.bottom = `${bottom}px`;
        }
    }
    // debounces updates, puts update processing on an animation frame
    let animationFrame = 0;
    function scheduleUpdate() {
        cancelAnimationFrame(animationFrame);
        animationFrame = requestAnimationFrame(update);
    }
    // listen for scroll or resize
    window.addEventListener('scroll', scheduleUpdate, { passive: true });
    window.addEventListener('resize', scheduleUpdate, { passive: true });
    // listen for manual content updates
    window.addEventListener('content-update', scheduleUpdate);
    // do initial update
    update();
    window.addEventListener('load', update, false);
    window.addEventListener('DOMContentLoaded', update, false);
    
    /**
    * Manually notify the affix system that a content update has occurred.
    */
    function notifyContentUpdated() {
        window.dispatchEvent(new CustomEvent('content-update'));
    }
    function getParentColumnWidth(element) {
        return element.parentElement.getBoundingClientRect().width;
    }
})();
// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let affix;
document.addEventListener('DOMContentLoaded', affix);