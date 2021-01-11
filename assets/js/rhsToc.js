/**
* Highlight correct RHS TOC Menu Item
**/

(function rhsToc() {
// initialise global variables outside functions
let observer = new IntersectionObserver(handler, { threshold: [0] });
let selection;
let headings = [...document.querySelectorAll("#main h2, #main h3")];
let rhsToc = [...document.querySelectorAll("ul.section-nav a")];
let a = null;
let headingMenuMap = headings.reduce((acc, h) => {
    let id = h.id;
    acc[id] = rhsToc.find(a => a.getAttribute("href") === "#" + id);
    return acc;
  }, {})

headings.forEach(elem => observer.observe(elem));

// detect scroll direction
scrollDetect();
let scrollDirection = [];
function scrollDetect(){
    var lastScroll = 0;
    window.onscroll = function() {
        let currentScroll = document.documentElement.scrollTop || document.body.scrollTop; // Get Current Scroll Value
        if (currentScroll > 0 && lastScroll <= currentScroll){
          lastScroll = currentScroll;
          scrollDirection = "down";
        }else{
          lastScroll = currentScroll;
          scrollDirection = "up"
        }
    };
  }
 
  
function handler(entries) {

    // Update selection with current entries.
    selection = (selection || entries).map( s => entries.find(e => e.target.id === s.target.id) || s);

    // keep only true values
    filteredArr = selection.filter(x => x.isIntersecting == true );

    // Find last visible/intersecting (use a copied array for that, since reverse is an in place method)
    let firstVisibleId = [...selection].find(x => x.isIntersecting) ? [...selection].find(x => x.isIntersecting).target.id : null;

    // Is a firstVisibleId returned? If not, then follow immediate steps below, otherwise skip this code block
    if (firstVisibleId === null & a!= null){
        // were you scrolling down? - then do nothing
        if (scrollDirection == "down"){
            // do nothing!
        } else {
            // scrolling up - so remove 'selected' from current menu item, and add it to the menu item above it
            const current = document.querySelector(`#side-doc-outline > ul li.selected`);
            if (current) {
                current.classList.remove('selected');
            }
            // if there is no previous sibling with a class of 'toc-entry', you're at the top of the branch, so go up a level, provided you don't get to section-nav
            if(previousByClass(a.parentElement, "toc-entry") == null){
                parent_by_selector(a.parentElement, "ul:not(.section-nav)") ? parent_by_selector(a.parentElement, "ul:not(.section-nav)").parentElement.classList.add("selected") : null;
            } else {
                previousByClass(a.parentElement, "toc-entry") ? previousByClass(a.parentElement, "toc-entry").classList.add("selected") : null;
            }
        }
        return;
    }

    // otherwise, remove 'selected' from the active item in the RHS toc
    const current = document.querySelector(`#side-doc-outline > ul li.selected`);
    if (current) {
        current.classList.remove('selected');
    }

    // Change class of Final ID
    for (s of selection) {
        let targetId = s.target.id;
        // get the entry from the generated map.
        a = headingMenuMap[targetId];
        if (firstVisibleId === targetId && a != null) {
            a.parentElement.classList.add("selected");
            return;
        } 
    };
}


// If item is selected from Menu (and is visible), override all of the above, and highlight that item instead
items = document.querySelectorAll(`.section-nav a`);

eventListeners();

function eventListeners(){
// Listen for click on any a tag link in rhs toc
Array.from(items).forEach(function(item) {
    item.addEventListener('click', itemClicked, false);
    });
}

function itemClicked(ev) {
    // Find last visible/intersecting (use a copied array for that, since reverse is an in place method)
    let firstVisibleId = [...selection].find(x => x.isIntersecting) ? [...selection].find(x => x.isIntersecting).target.id : null;

    // Choose between firstvisibleID or clickedonID
    let clickedOnId = ev.target.getAttribute("href").substring(1);
    let clickedIdVisible = selection.find(e => e.target.id === clickedOnId).isIntersecting;
    let finalId = null;
    

    // set Final ID
    if (clickedIdVisible){
        finalId = clickedOnId;
    } else {
        finalId = firstVisibleId;
    }

    // Remove 'selected' from the active item in the RHS toc
    const current = document.querySelector(`#side-doc-outline > ul li.selected`);
    if (current) {
        current.classList.remove('selected');
    }

    // Change class of Final ID
    for (s of selection) {
        let targetId = s.target.id;
        // get the entry from the generated map.
        a = headingMenuMap[targetId];
        if (finalId === targetId) {
            a.parentElement.classList.add("selected");
            return;
        } 
    };
}

})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let rhsToc;
document.addEventListener('DOMContentLoaded', rhsToc);