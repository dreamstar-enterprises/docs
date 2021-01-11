/**
* Generate rest of Breadcrumb, based on where you are in the Navigation Menu
**/

"use strict";
(function breadCrumb() {

generateBreadCrumb();
function generateBreadCrumb() {
    let df = new DocumentFragment(); 
    let breadcrumbs = document.getElementById("page-breadcrumbs");
    let target = window.location.pathname;
    
    // Find first "a" with the target as href
    let a = [...document.querySelectorAll(".toc a")].find(a => a.pathname === target)
    let parent = a;

    // Only do if link is found in LHS Tree
    if(a){
        // Do this if already at Top-Most Level
        if (!parent.parentElement.closest(".treegroup").previousElementSibling){
            // Initialise variables for HTML
            let txtValue =  document.createTextNode(a.textContent);
            let linkValue = a.getAttribute("href");
            let li_bc = document.createElement('li');
            let a_bc = document.createElement('a');
            let span_bc = document.createElement("span");
            
            // Create HTML
            span_bc.appendChild(txtValue);
            a_bc.appendChild(span_bc);
            a_bc.setAttribute("href", linkValue);
            li_bc.appendChild(a_bc);
            df.appendChild(li_bc);
        }

        // Do this if not at Top-Most Level
        while (parent = parent.parentElement.closest(".treegroup")) {
            if (parent.previousElementSibling){

                // Initialise variables for HTML
                let txtValue =  document.createTextNode(parent.previousElementSibling.firstElementChild.textContent);
                let linkValue = parent.firstElementChild.firstElementChild;
                let li_bc = document.createElement('li');
                let a_bc = document.createElement('a');
                let span_bc = document.createElement("span");

                // Create HTML
                span_bc.appendChild(txtValue);
                a_bc.appendChild(span_bc);
                if(linkValue.tagName == 'A') a_bc.setAttribute("href", linkValue);
                li_bc.appendChild(a_bc);
                df.insertBefore(li_bc, df.childNodes[0]);
            }
        };

        breadcrumbs.appendChild(df);
    }
}

})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let breadCrumb;
document.addEventListener('DOMContentLoaded', breadCrumb);
