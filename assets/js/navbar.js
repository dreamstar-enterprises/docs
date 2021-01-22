/**
* Apply JS to NavBar elements
**/

"use strict";
(function navbar() {

    // Initiate and assign Global Variables
    let page_title = [...document.querySelectorAll("#main h1")][0] ? [...document.querySelectorAll("#main h1")][0].textContent : null;
    let page_url = window.location.protocol + "//" + window.location.host + "/" + window.location.pathname;

    // Function for highlighting active NavBar and BreadCrumb links
    highlightNavBar()
    function highlightNavBar() {
        let target = window.location.pathname;

        // Find first "a" and "b" with the target as href
        let navbar = [...document.querySelectorAll(".nav-bar-nav-list a")].find(a => a.pathname === target) 
        if(navbar){
            navbar.classList.add("is-active")
        }

        let breadcrumb = [...document.querySelectorAll(".breadcrumbs a")].find(a => a.pathname === target)
        if(breadcrumb){
            breadcrumb.parentElement.classList.add("is-active")
        }
    }

    // Function for generating and embedding Social Media and Email links
    generateShareLinks ()
    function generateShareLinks() {

        // Get Elements to Modify
        let twitter = [...document.querySelectorAll("#twitter-link a")][0];
        let linkedin = [...document.querySelectorAll("#linkedin-link a")][0];
        let facebook = [...document.querySelectorAll("#facebook-link a")][0];
        let email = [...document.querySelectorAll("#email-link a")][0];

        // Generate Shareable Links
        let twitter_share = "https://twitter.com/intent/tweet?original_referer=https://www.startech-enterprises.com&text=" + page_title + ":&tw_p=tweetbutton&url=" + page_url;
        let linkedin_share = "https://www.linkedin.com/sharing/share-offsite/?url=" + page_url;
        let facebook_share = "https://www.facebook.com/sharer/sharer.php?u=" + page_url;
        let email_share = "mailto:?subject=StarBase: [Shared Article] " + page_title + "&body=" + page_title + ":%0A%0A" + page_url;
        
        // Embed links within Elements
        twitter.setAttribute("href", twitter_share);
        linkedin.setAttribute("href", linkedin_share);
        facebook.setAttribute("href", facebook_share);
        email.setAttribute("href", email_share);
   
    }

    // Function for showing and hiding share menu
    toggleShareMenu()
    // Toggle when Share Button is clicked
    function toggleShareMenu() {
        // Get Element to Modify
        let d = [...document.querySelectorAll("#share-menu-link button")];

        d[0].addEventListener('click', function(ev){
            event.stopPropagation();
            let sharingMenu = document.querySelector("#sharing-menu");

            if (sharingMenu.classList.contains("is-hidden")){
                sharingMenu.classList.remove("is-hidden");
            } else {
                sharingMenu.classList.add("is-hidden");
            }
        }, false);

        // Hide if anywhere else in page is clicked
        document.getElementsByTagName("html")[0].addEventListener('click', function() {
        let sharingMenu = document.querySelector("#sharing-menu");
            if (!sharingMenu.classList.contains("is-hidden")){
                sharingMenu.classList.add("is-hidden");
            }
        })
    }

    // When the user scrolls the page, execute scrollIndicator
    window.addEventListener("scroll", function () {
        scrollIndicator(); 
    }, false);

    let winheight = document.documentElement.scrollHeight;
    let docheight = document.documentElement.clientHeight;

    function scrollIndicator() {
        let winScroll =  document.documentElement.scrollTop;
        let scrolled = (winScroll / (winheight - docheight)) * 100;
        document.getElementById("myBar").style.width = scrolled + "%";
    } 

})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let navbar;
document.addEventListener('DOMContentLoaded', navbar);