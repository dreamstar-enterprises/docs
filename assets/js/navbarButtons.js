/**
* Apply JS to NavBarButtons elements
**/

"use strict";
(function navbarButtons() {

    // When the user scrolls the page, execute navbarEffect
    window.addEventListener("scroll", function () {
        navbarEffect();
    }, false);

    let winScroll = 0;
    let navbarButtons = document.getElementsByClassName('nav-bar-item');
    navbarButtons[0].firstElementChild.style.fontSize = "1.025rem";

    navbarButtons[1].firstElementChild.style.fontSize = "0.800rem";
    navbarButtons[2].firstElementChild.style.fontSize = "0.800rem";
    navbarButtons[3].firstElementChild.style.fontSize = "0.800rem";
    navbarButtons[4].firstElementChild.style.fontSize = "0.800rem";

    function navbarEffect() {
        // When the user scrolls
        winScroll =  document.documentElement.scrollTop;
        
        console.log(winScroll);

        navbarButtons[0].firstElementChild.firstElementChild;

        if(winScroll > 110) {
            navbarButtons[0].firstElementChild.style.fontSize = "1.100rem";

            navbarButtons[1].firstElementChild.style.fontSize = "0.875rem";
            navbarButtons[2].firstElementChild.style.fontSize = "0.875rem";
            navbarButtons[3].firstElementChild.style.fontSize = "0.875rem";
            navbarButtons[4].firstElementChild.style.fontSize = "0.875rem";
        } else {
            navbarButtons[0].firstElementChild.style.fontSize = "1.025rem";

            navbarButtons[1].firstElementChild.style.fontSize = "0.800rem";
            navbarButtons[2].firstElementChild.style.fontSize = "0.800rem";
            navbarButtons[3].firstElementChild.style.fontSize = "0.800rem";
            navbarButtons[4].firstElementChild.style.fontSize = "0.800rem";
        }
    };


})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let navbarButtons;
document.addEventListener('DOMContentLoaded', navbarButtons);    