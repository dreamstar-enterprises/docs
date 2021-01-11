/**
* Expand or collapse menu items
**/

"use strict";
(function lhsTree() {
    let menu = document.querySelector('.toc');
    let elements = menu.getElementsByClassName("treeitem");
    let sibling = null;
    let expanded = false;

    eventListeners();
    function eventListeners(){
        // Listen for click
        Array.from(elements).forEach(function(element) {
            element.addEventListener('click', function(ev) {
                let e = null;
                ev.target.classList.contains("treeitem") ? e = ev.target : e = parent_by_class(ev.target, "treeitem");
                sibling = nextByClass(e, "treegroup")

                sibling.classList.contains('is-expanded') ? expanded = true : expanded = false;  
                    if(expanded){
                        e.classList.remove("is-expanded");
                        sibling.classList.remove("is-expanded");
                    } else {
                        e.classList.add("is-expanded");
                        sibling.classList.add("is-expanded");
                    } 
            }, false);
          });
    }

    // Get window location pathname
    let target = window.location.pathname;
    // Find first "a" with the target as href
    let a = [...document.querySelectorAll(".toc a")].find(a => a.pathname === target);
    // Get Previous Element
    let prior = a.parentElement.previousElementSibling;
    // Get Next Element
    let next = a.parentElement.nextElementSibling;
    // Get DOM Elements
    let next_button = document.getElementById("nextPage");
    let previous_button = document.getElementById("previousPage");

    expandEntryOnLoad();
    function expandEntryOnLoad() {
        // Expand all tree group parents.
        if (a) {
          a.parentElement.classList.add("is-active");
          let parent = a;
          while (parent = parent.parentElement.closest(".treegroup")) parent.classList.add("is-expanded");
          a.scrollIntoView({behavior: "smooth", block: "center", inline: "nearest"});
        }
    }

    getPreviousMenuItem();
    function getPreviousMenuItem() {
        // Does Previous Element exist?
        if (!prior){
            // Go up one level to tree-group
            prior = parent_by_class(a.parentElement, "treegroup");
            // Go to relevant previous element
            while(true) {
                if (prior.previousElementSibling != null && prior.previousElementSibling.previousElementSibling != null){
                    if (prior.previousElementSibling.previousElementSibling.classList.contains("none")) break;
                    if (prior.previousElementSibling.previousElementSibling.classList.contains("treegroup")) break;
                };   
                prior = prior.parentElement;
                if (prior.classList.contains("toc") || prior.parentElement.classList.contains("toc")) break;
            }
           
            prior = prior.previousElementSibling ? prior.previousElementSibling.previousElementSibling ? prior.previousElementSibling.previousElementSibling : null : null;
            
            // Are you at the top of the tree
            if (!prior){
                prior = null;
            } else if (prior.classList.contains("none")) {
                // Get previous Element
                prior = prior.firstElementChild;
            } else if (prior.classList.contains("treegroup")){
                // Go all the way down to lowest level
                while(true){
                    if (prior.lastElementChild.classList.contains('none')) break;
                    prior = prior.lastElementChild;
                };
                prior = prior.lastElementChild.firstElementChild;
            }
        } else if (prior.classList.contains("none")){
            // Get previous Element
            prior = prior.firstElementChild;
        } else if (prior.classList.contains("treegroup")){
            // Go all the way down to lowest level
            while(true){
                if (prior.lastElementChild.classList.contains('none')) break;
                prior = prior.lastElementChild;
            };
            prior = prior.lastElementChild.firstElementChild;
        }
    }
   
    getNextMenuItem();
    function getNextMenuItem() {
        // Does Next Element exist?
        // if first child Element doesn't exist, either stop, or try to get first element of next immediate branch
        if(!next){
            next = a.parentElement;
            while(true){
                if (next.parentElement.nextElementSibling) break;
                next = next.parentElement
                if (next.parentElement.classList.contains("toc")) break;
            };
            next = next.parentElement.nextElementSibling ? next.parentElement.nextElementSibling : null;
            // Does Next Element exist
            if(!next){
                next = null;
            } else if (next.classList.contains("none")){
                // Get Next Element
                next = next.firstElementChild;
            } else if (next.classList.contains("treeitem")){
                // Goto Next Sibling Element
                next = next.nextElementSibling;
                while(true){
                    next = next.firstElementChild;
                    if (next.classList.contains("none")) break;
                    next = next.nextElementSibling;
                };
                // Get first child Element
                next = next.firstElementChild;
            }
        } else if (next.classList.contains("none")){
            // Get Next Element
            next = next.firstElementChild;
        } else if (next.classList.contains("treeitem")){
            // Goto Next Sibling Element
            next = next.nextElementSibling;
            while(true){
                next = next.firstElementChild;
                if (next.classList.contains("none")) break;
                next = next.nextElementSibling;
            };
            // Get first child Element
            next = next.firstElementChild;
        }
    }  

    // Modify HTML of next and back buttons

    next ? next_button.parentElement.setAttribute("href", next) : next_button.parentElement.setAttribute("href", "#top");
    prior ? previous_button.parentElement.setAttribute("href", prior): previous_button.parentElement.setAttribute("href", "#top");
    
    next ? next_button.classList.remove("is-faded") : next_button.classList.add("is-faded");
    prior ? previous_button.classList.remove("is-faded") : previous_button.classList.add("is-faded");
    
})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let lhsTree;
document.addEventListener('DOMContentLoaded', lhsTree);
