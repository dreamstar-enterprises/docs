/**
* Search Filter
**/

"use strict";
(function searchFilter() {

    let input = document.getElementById('searchFilter');
    let suggestions = document.getElementsByClassName("searchFilter-suggestions")[0];

    eventListeners();

     // Add Event Listerns
    function eventListeners() {
      input.addEventListener('keyup', searchQuery);
      input.addEventListener("mouseenter", () => removeInputFocusListeners());
      input.addEventListener("mouseleave", () => addInputFocusListeners());

      suggestions.addEventListener("mouseenter", () => removeInputFocusListeners());
      suggestions.addEventListener("mouseleave", () => addInputFocusListeners());
    };

    function addInputFocusListeners() {
      input.addEventListener('focusout', searchQuery);
      input.addEventListener('focusin', searchQuery);
    }

    function removeInputFocusListeners() {
      input.removeEventListener('focusout', searchQuery);
      input.removeEventListener('focusin', searchQuery);
    }

    
    function searchQuery(){

        // Declare variables
        let input, filter, ul_toc, li_toc, ul_suggestions, li_suggestion, a1, a2, a3, i, j, k, txtValue, txtValue2, txtValue3, link;

        input = document.getElementById('searchFilter');
        filter = input.value;
        ul_toc = document.getElementsByClassName("toc")[0];
        li_toc = ul_toc.getElementsByClassName("none");
        ul_suggestions = document.getElementsByClassName("searchFilter-suggestions")[0];

        // Check whether input is empty. If so hide UL Element
        if (filter === "") {
            ul_suggestions.classList.add("is-hidden")
        };

        // Check whether input is not empty. If so show UL Element
        if (filter !== "") {
            ul_suggestions.classList.remove("is-hidden")
        };

        // Check whether input is not active. If so hide UL Element
        if (input !== document.activeElement) {
            setTimeout(function(){
            ul_suggestions.classList.add("is-hidden");
            }, 100);
        };

        // Check whether input is active. If so show UL Element
        if (input === document.activeElement) {
        ul_suggestions.classList.remove("is-hidden")
        };

        // Keep emptying UL on each keyup event, or when input element is not active
        ul_suggestions.innerHTML = "";

        let df = new DocumentFragment(); 
        
        // Run search query but only if filter is not an empty string
        if(filter.trim()){
            // Loop through all list items, and update document fragment for those that match the search query
            for (i = 0; i < li_toc.length; i++) {
                a1 = li_toc[i].getElementsByTagName("a")[0];
                txtValue = a1.textContent || a1.innerText;

                let textArray = filter.match(/\b(\w+)\b/g);
                if (matchWordsPartial(txtValue, textArray).length > 0) {  

                    // Start creating internal HTML
                    li_suggestion = document.createElement('li');
                    li_suggestion.classList.add("searchFilter-suggestion");

                    // Parent span element
                    let span = document.createElement("SPAN");
                    span.className = ("is-block is-size-7 has-padding-left-small has-padding-right-small");
                    link = document.createElement('a');
                    link.href = a1.href;
                    span.appendChild(link);
                       
                        // Child 1 span element
                        let span2 = document.createElement("SPAN");
                            span2.className = ("is-block has-overflow-ellipsis-tablet");
                            span2.textContent = txtValue;

                        // Child 2 span element
                        let span3 = document.createElement("SPAN");
                            span3.className = ("is-block has-text-subtle has-overflow-ellipsis is-size-8 has-line-height-reset has-padding-bottom-extra-small");
                            
                            j = 0;
                            let immediateParent = li_toc[i].parentElement;
                            let correctParent = li_toc[i].parentElement;

                            // Get top most level of branch --> Set as Node 1
                            while(true){
                                if (immediateParent.parentElement.classList.contains('toc')) break;
                                immediateParent = immediateParent.parentElement;
                                j++;
                            };
                            if (j == 0){
                                a2 = li_toc[i].getElementsByTagName("a")[0];
                            } 
                            else {
                                k = 0;
                                for ( k = 0; k < j - 1; k++) {
                                    correctParent = correctParent.parentElement;
                                };

                                a2 = previousByClass(correctParent, "treeitem");
                                a2 = child_by_selector(a2, "tree-expander")
                            }
                            txtValue2 = a2.textContent;
                            txtValue2 = document.createTextNode(txtValue2);

                            // Insert Chevron Right --> Set as Node 2
                            let span4 = document.createElement("SPAN");
                            span4.className = ("has-padding-right-extra-small has-padding-left-extra-small");
                            span4.innerHTML =  '&nbsp&#9002&nbsp';
                            span4.setAttribute("style", "font-size: 0.70rem; font-weight: bold");
                            
                            // Get second-top most level of branch --> Set as Node 2
                            correctParent = li_toc[i].parentElement;
                            
                            switch (j) {
                                case 0:
                                    a3 = "";
                                break;
                                case 1:
                                    a3 = li_toc[i].getElementsByTagName("a")[0];
                                break;
                                default: {
                                    k = 0;
                                    for ( k = 0; k < j - 2; k++) {
                                        correctParent = correctParent.parentElement;
                                    };

                                    a3 = previousByClass(correctParent, "treeitem");
                                    a3 = child_by_selector(a3, "tree-expander")
                                    }
                                break;
                                }   

                            if (a3 != ""){
                                txtValue3 = a3.textContent;
                                txtValue3 = document.createTextNode(txtValue3);
                                span3.appendChild(txtValue2);
                                span3.appendChild(span4);
                                span3.appendChild(txtValue3);
                            } else {
                                span3.appendChild(txtValue2);
                            }
                        span.firstChild.appendChild(span2);
                        span.firstChild.appendChild(span3); 

                    li_suggestion.appendChild(span);
                    df.appendChild(li_suggestion);
                } 
            }
             // Output HTML
             ul_suggestions.appendChild(df);
        }
    }

    // Search Algorithms
	// Match Input Word Array to Search String (even Partial Word Mathes)
	var matchWordsPartial = function (searchstring, word_Array) {
		var regexMetachars = /[(){[*+?.\\^$|]/g;
		for (var i = 0; i < word_Array.length; i++) {
			word_Array[i] = '(?=.*' + word_Array[i].replace(regexMetachars, "\\$&") + '.*)';
		}
		var regex = new RegExp(word_Array.join(''), "gi");
		return searchstring.match(regex) || [];
	}
})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let searchFilter;
document.addEventListener('DOMContentLoaded', searchFilter);