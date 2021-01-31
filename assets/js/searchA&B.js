
"use strict";

(function searchAandB() {

	// Declare & Assign Variables
	const topForm = document.querySelector('#top-form');
	const topInput = document.querySelector('#top-search');
	const resultList = document.querySelector('#search-results');
	const paginationElement = document.querySelector('#pagination');
	let items_per_page = 9;
	let current_Page = 1;
	let logicSwitch = document.querySelector('#logic-type');
	if(sessionStorage.getItem("logicSwitch") === 'false'){
		logicSwitch.checked = ""
		logicSwitch.parentElement.nextElementSibling.innerHTML ='<i>Search: <b>OR</b></i>';
	}else{
		logicSwitch.checked = "checked";
	}
    
	const yearItems = [...document.querySelectorAll(".year-item")];
	const monthItems = [...document.querySelectorAll(".month-item")];
	const typeItems = [...document.querySelectorAll(".type-item")];
	const filterItemsContainer = document.querySelector("#filter-items-container");
	const filterClear = document.querySelector('#filter-clear');
	const filterClear2 = document.querySelector('#filter-clear-2');
	const filterClear3 = document.querySelector('#filter-clear-3');
	const filterClear4 = document.querySelector('#filter-clear-4');

	// put all Year Items into a single Array
	let yearValues = [];
	let yearNames =[];
	for (let i = 0; i < yearItems.length; i++) {
		yearValues.push(yearItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		yearNames.push(yearItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray = [];
	if (sessionStorage.getItem("tagArray")) {
		// Get checked items from Session Storage
		checkedItemArray = JSON.parse(sessionStorage.getItem("tagArray"));
	  }

	// put all Month Items into a single Array
	let monthValues = [];
	let monthNames =[];
	for (let i = 0; i < monthItems.length; i++) {
		monthValues.push(monthItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		monthNames.push(monthItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray2 = [];
	if (sessionStorage.getItem("tagArray2")) {
		// Get checked items from Session Storage
		checkedItemArray2 = JSON.parse(sessionStorage.getItem("tagArray2"));
	  }

	// put all Type Items into a single Array
	let typeValues = [];
	let typeNames =[];
	for (let i = 0; i < typeItems.length; i++) {
		typeValues.push(typeItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		typeNames.push(typeItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray3 = [];
	if (sessionStorage.getItem("tagArray3")) {
		// Get checked items from Session Storage
		checkedItemArray3 = JSON.parse(sessionStorage.getItem("tagArray3"));
      }
      

	// Count total number of checked items
	var countTotalCheckedItems = function (){
		let count = 0;
		let count2 = 0;
		let count3 = 0;

		// Count Year Items
		yearValues.forEach(function (yearValue, i) {
			if(yearItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
			}
        });
        // Show Clear Year Filter, if at least one item is checked
        if (count > 0) {
            document.querySelector('#clear-filters').classList.remove('is-hidden');
        } else{
            document.querySelector('#clear-filters').classList.add('is-hidden');
        }

		// Count Month Items
		monthValues.forEach(function (monthValue, i) {
			if(monthItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
			}
        });
        // Show Clear Month Filter, if at least one item is checked
        if (count2 > 0) {
            document.querySelector('#clear-filters-2').classList.remove('is-hidden');
        } else{
            document.querySelector('#clear-filters-2').classList.add('is-hidden');
        }

		// Count Type Items
		typeValues.forEach(function (typeValue, i) {
			if(typeItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
			}
        });
        // Show Clear Type Filter, if at least one item is checked
        if (count3 > 0) {
            document.querySelector('#clear-filters-3').classList.remove('is-hidden');
        } else{
            document.querySelector('#clear-filters-3').classList.add('is-hidden');
        }

		// Also, if at least one item is checked, show 'filter clear' button
		if(count > 0 || count2 >0 || count3 >0){
			filterClear.classList.remove('is-hidden');
		} else {
			filterClear.classList.add('is-hidden');
		}

	};


	// Count number of checked items and create Tags
	var countCheckedItems = function (){
		let count = 0;
		let count2 = 0;
		let count3 = 0;

		filterItemsContainer.innerHTML = "";
		while (checkedItemArray.length > 0) {
			checkedItemArray.shift();
		}
		while (checkedItemArray2.length > 0) {
			checkedItemArray2.shift();
		}
		while (checkedItemArray3.length > 0) {
			checkedItemArray3.shift();
		}

		// Also, create Year Tags
		yearValues.forEach(function (yearValue, i) {
			if(yearItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-year"' + ' id="'+ yearNames[i] + '"' + '>' + 
					'<span>' +
						yearValue +
					'</span>' +
					'<span class="delete">' +
                    '</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray.push(yearValue);
			}
		});
		sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));

		// Also, create Month Tags
		monthValues.forEach(function (monthValue, i) {
			if(monthItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-month"' + ' id="'+ monthNames[i] + '"' + '>' + 
					'<span>' +
						monthValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray2.push(monthValue);
			}
		});
		sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));

		// Also, create Type Tags
		typeValues.forEach(function (typeValue, i) {
			if(typeItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-type"' + ' id="'+ typeNames[i] + '"' + '>' + 
					'<span>' +
						typeValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray3.push(typeValue);
			}
		});
		sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
		
		// Also, add Event Listeners to Year Tags
		yearValues.forEach(function (yearValue, i) {
			if(yearItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + yearNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + yearNames[i]).addEventListener('click', TagClick(yearNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Month Tags
		monthValues.forEach(function (monthValue, i) {
			if(monthItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + monthNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + monthNames[i]).addEventListener('click', TagClick2(monthNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Type Tags
		typeValues.forEach(function (typeValue, i) {
			if(typeItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + typeNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + typeNames[i]).addEventListener('click', TagClick3(typeNames[i], i), false);
			}
		});

		// Also, if at least one item is checked, show 'filter clear' button(s)
		countTotalCheckedItems();

		// Do search
		search(topInput.value, 1);
	}


	// Uncheck Checkboxes, and remove Year Tag, when Tag is clicked
	var TagClick = function (tag, i){
		
		return function (){
			let count = 0;
			while (checkedItemArray.length > 0) {
				checkedItemArray.shift();
			}

			// Uncheck relevant CheckBox 
			yearItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items
			yearValues.forEach(function (yearValue, i) {
				if(yearItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray.push(yearValue);
				}
			});
			sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));
			current_Page = 1;
			
			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};

	// Uncheck Checkboxes, and remove Month Tag, when Tag is clicked
	var TagClick2 = function (tag, i){
	
		return function (){
			let count = 0;

			while (checkedItemArray2.length > 0) {
				checkedItemArray2.shift();
			}

			// Uncheck relevant CheckBox 
			monthItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items

			monthValues.forEach(function (monthValue, i) {
				if(monthItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray2.push(monthValue);
				}
			});
			sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));
			current_Page = 1;

			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};

	// Uncheck Checkboxes, and remove Type Tag, when Tag is clicked
	var TagClick3 = function (tag, i){
	
		return function (){
			let count = 0;

			while (checkedItemArray3.length > 0) {
				checkedItemArray3.shift();
			}

			// Uncheck relevant CheckBox 
			typeItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items

			typeValues.forEach(function (typeValue, i) {
				if(typeItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray3.push(typeValue);
				}
			});
			sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
			current_Page = 1;

			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};	


	// Clear all Filters
	var clearFilter = function (){
		yearValues.forEach(function (yearValue, i) {
			yearItems[i].classList.remove('is-hidden')
			yearItems[i].firstElementChild.firstElementChild.checked = false;
		});

		monthValues.forEach(function (monthValue, i) {
			monthItems[i].classList.remove('is-hidden')
			monthItems[i].firstElementChild.firstElementChild.checked = false;
		});

		typeValues.forEach(function (typeValue, i) {
			typeItems[i].classList.remove('is-hidden')
			typeItems[i].firstElementChild.firstElementChild.checked = false;
		});

        filterClear.classList.add('is-hidden');
        document.querySelector('#clear-filters').classList.add('is-hidden');
		document.querySelector('#clear-filters-2').classList.add('is-hidden');
		document.querySelector('#clear-filters-3').classList.add('is-hidden');

		filterItemsContainer.innerHTML = "";

		while (checkedItemArray.length > 0) {
			checkedItemArray.shift();
		  }
		
		while (checkedItemArray2.length > 0) {
		checkedItemArray2.shift();
		}

		while (checkedItemArray3.length > 0) {
		checkedItemArray3.shift();
		}

		sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));
		sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));
		sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
		sessionStorage.setItem("page", 1);
		topInput.value = '';
		sessionStorage.setItem("topInput", topInput.value);
		sessionStorage.setItem("logicSwitch", true);
		current_Page = 1;
		
		// Do search
		search(topInput.value, 1);
	}

	// Clear all Year Filters
	var clearFilter2 = function (){
		yearValues.forEach(function (currentValue, i) {
			yearItems[i].classList.remove('is-hidden')
			yearItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#clear-filters').classList.add('is-hidden');

		// Remove Year Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-year')		
		for (let i = checkedItems.length - 1; i >= 0; i--){
			checkedItems[i].remove();
		};

		while (checkedItemArray.length > 0) {
			checkedItemArray.shift();
			}
		sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));
		current_Page = 1;
		
		// Also, if at least one item is checked, show the 'filter clear' button
		countTotalCheckedItems();

		// Do search
		search(topInput.value, 1);
	}

	// Clear all Month Filters
	var clearFilter3 = function (){
		monthValues.forEach(function (currentValue, i) {
			monthItems[i].classList.remove('is-hidden')
			monthItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#clear-filters-2').classList.add('is-hidden');

		// Remove Month Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-month')		
		for (let i = checkedItems.length - 1; i >= 0; i--){
			checkedItems[i].remove();
		};

		while (checkedItemArray2.length > 0) {
			checkedItemArray2.shift();
			}
		sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));
		current_Page = 1;
		
		// Also, if at least one item is checked, show the 'filter clear' button
		countTotalCheckedItems();

		// Do search
		search(topInput.value, 1);
	}

	// Clear all Type Filters
	var clearFilter4 = function (){
		typeValues.forEach(function (currentValue, i) {
			typeItems[i].classList.remove('is-hidden')
			typeItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#clear-filters-3').classList.add('is-hidden');

		// Remove Type Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-type')		
		for (let i = checkedItems.length - 1; i >= 0; i--){
			checkedItems[i].remove();
		};

		while (checkedItemArray3.length > 0) {
			checkedItemArray3.shift();
			}
		sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
		current_Page = 1;
		
		// Also, if at least one item is checked, show the 'filter clear' button
		countTotalCheckedItems();

		// Do search
		search(topInput.value, 1);
	}	

    // Change logic switch
	var changeLogicSwitch= function () {
		logicSwitch.parentElement.nextElementSibling.innerText ="";
		if(logicSwitch.checked){
			logicSwitch.parentElement.nextElementSibling.innerHTML ='<i>Search: <b>AND</b></i>';
		} else{
			logicSwitch.parentElement.nextElementSibling.innerHTML ='<i>Search: <b>OR</b></i>';
		}
		sessionStorage.setItem("logicSwitch", logicSwitch.checked);
		current_Page = 1;

		// Do search
		search(topInput.value, 1);
	};

	// Remove site: from the input
	var clearInput = function () {
		topInput.value = topInput.value.replace(' StarBase Docs', '');
	};


	// Handle submit events
	var submitHandler = function (event) {
		event.preventDefault();
		search(topInput.value, 1);
	};


	// Search for matches
	var search = function (query, page) {

		// Variables
		current_Page = parseInt(page);
		sessionStorage.setItem("topInput", query.trim());
		var priority0 = []; // for search in year, month, and type tag fields
		var priority1 = []; // for search in title & content fields
		var results = [];

		// Search Year, Month, & Type Tags
		if(checkedItemArray.length > 0 || checkedItemArray2.length > 0 || checkedItemArray3.length > 0){
			let tagArray = checkedItemArray;
			let tagArray2 = checkedItemArray2;
			let tagArray3 = checkedItemArray3;
			searchIndex.forEach(function (article) {
				let match1 = false;
				let match2 = false;
				let match3 = false;
				if(logicSwitch.checked){
					if (!Array.isArray(tagArray) || !tagArray.length) match1 = true;
					if (!Array.isArray(tagArray2) || !tagArray2.length) match2 = true;
					if (!Array.isArray(tagArray3) || !tagArray3.length) match3 = true;
					for (let i=0; i < article.tags_year.length; i++) {
						tagArray.forEach(function (tag){
							if(article.tags_year[i] == tag) match1 = true;
						});
					}
					for (let i=0; i < article.tags_month.length; i++) {
						tagArray2.forEach(function (tag){
							if(article.tags_month[i] == tag) match2 = true;
						});
					}
					for (let i=0; i < article.tags_type.length; i++) {
						tagArray3.forEach(function (tag){
							if(article.tags_type[i] == tag) match3 = true;
						});
					}
					if (match1 && match2 && match3) priority0.push(article);
				} else {
					for (let i=0; i < article.tags_year.length; i++) {
						tagArray.forEach(function (tag){
							if(article.tags_year[i] == tag) match1 = true;
						});
					}
					for (let i=0; i < article.tags_month.length; i++) {
						tagArray2.forEach(function (tag){
							if(article.tags_month[i] == tag) match2 = true;
						});
					}
					for (let i=0; i < article.tags_type.length; i++) {
						tagArray3.forEach(function (tag){
							if(article.tags_type[i] == tag) match3 = true;
						});
					}
					if (match1 || match2 || match3) priority0.push(article);
				}
			});
		} else {
			priority0 = searchIndex;
		}
		

		// Do Search, provided Query String is not Empty, or at least one Filter Item is checked. Otherwise, return All Results
		if(query.trim()){
			priority0.forEach(function (article) {
				// Convert Input String into Array of Words
				if(matchWordsPartial(article.title.concat(' ', article.content), query.match(/\b(\w+)\b/g)).length > 0) priority1.push(article);
			});
			results = priority1;
		} else {
			results = priority0;
		}


		// Add Article Counts by Year to Page
			// Show Years for all Matched Articles
			let yearArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_year.length; i++) {
					yearValues.forEach(function (year){
						if(article.tags_year[i] == year) yearArray.push(article.tags_year[i]);
					});
				}
			});

			// Show Article Count Grouped by Year
			let yearObjectCount = yearArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from Year Items in HTML
			yearValues.forEach(function (currentValue, i) {
				yearItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all Year Items in HTML
			Object.keys(yearObjectCount).forEach(function(key) {
				let yearElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-y";
				let yearElement = document.getElementById(yearElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				yearElement.innerHTML = "&nbsp (" + yearObjectCount[key] + ")";
			});

		// Add Article Counts by Month to Page
			// Show Months for all Matched Articles
			let monthArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_month.length; i++) {
					monthValues.forEach(function (month){
						if(article.tags_month[i] == month) monthArray.push(article.tags_month[i]);
					});
				}
			});

			// Show Article Count Grouped by Month
			let monthObjectCount = monthArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from Month Items in HTML
			monthValues.forEach(function (currentValue, i) {
				monthItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all Month Items in HTML
			Object.keys(monthObjectCount).forEach(function(key) {
				let monthElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-m";
				let monthElement = document.getElementById(monthElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				monthElement.innerHTML = "&nbsp (" + monthObjectCount[key] + ")";
			});

		// Add Article Counts by Type to Page
			// Show Types for all Matched Articles
			let typeArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_type.length; i++) {
					typeValues.forEach(function (type){
						if(article.tags_type[i] == type) typeArray.push(article.tags_type[i]);
					});
				}
			});

			// Show Article Count Grouped by Type
			let typeObjectCount = typeArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from Type Items in HTML
			typeValues.forEach(function (currentValue, i) {
				typeItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all Type Items in HTML
			Object.keys(typeObjectCount).forEach(function(key) {
				let typeElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-type";
				let typeElement = document.getElementById(typeElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				typeElement.innerHTML = "&nbsp (" + typeObjectCount[key] + ")";
			});

		// Display the results
		displayList(results, resultList, items_per_page, current_Page);

	};

	// Display results
	var displayList = function(items, wrapper, items_per_page, page){
		sessionStorage.setItem("page", page);
		page--;

		let start = items_per_page * page;
		let end = start + items_per_page;
		let paginatedItems = items.slice(start, end);

		wrapper.innerHTML = "";
		wrapper.innerHTML = items.length < 1 ? createNoResultsHTML() : createResultsHTML(paginatedItems, items);

		// Display pagination buttons
		setupPagination(items, paginationElement, items_per_page)

	}

	// Create the markup when no results are found
	var createNoResultsHTML = function () {
		return '<div class="has-margin-top-large is-text-centered has-margin-bottom-medium">' +
					'<figure class="has-margin-bottom-medium">' +
						'<img src="images/no-results.svg">' +
					'</figure>' +
					'<h2 class="title is-2 has-margin-bottom-medium">' +
					'No Results' +
					'</h2>' +
					'<p>' +
					'Sorry, we couldn\'t find a fit to your search. Please try another search.' +
					'</p>' +
				'</div>';
	};

	// Create the markup for results
	var createResultsHTML = function (paginatedResults, allResults) {
		var html = '<div class="has-margin-top-large">' +
						'<h2 class="title is-6"; style="border-bottom: #C8C8C8; border-bottom-style: solid; line-height: 3em; border-bottom-width: thin;">'
							+ allResults.length + ' result(s)' +
						'</h2>' +
					'</div>' +
					'<ul class="grid has-margin-top-large">';
		html += paginatedResults.map(function (article, index) {
			return createHTML(article, index);
		}).join('');
		html += '</ul>';
		return html;
	};

	// Create the HTML for each result
	var createHTML = function (article, id) {
		var tags_year = "";
		for (let i=0; i < article.tags_year.length; i++) {
		tags_year += '<li class="tag-year is-small">' + article.tags_year[i] + '</li>';
		};

		var tags_month = "";
		for (let i=0; i < article.tags_month.length; i++) {
		tags_month += '<li class="tag-month is-small">' + article.tags_month[i] + '</li>';
		};

		var tags_type = "";
		for (let i=0; i < article.tags_type.length; i++) {
		tags_type += '<li class="tag-type is-small">' + article.tags_type[i] + '</li>';
		};

		var html =
				'<li class="grid-item articles-and-blogs">' +
					'<article class="card articles-and-blogs">' +
						'<div class="card-content">' +
							'<a href="' + article.url + '">' +
								'<img src="' + article.image_url + '">' +
							'</a>' +
							'<a class="card-content-title articles-and-blogs" href="' + article.url + '">' +
								'<h3>' + article.title + '</h3>' +
							'</a>' +
							'<ul class="card-content-metadata articles-and-blogs">' +
								'<li>' +
									'<time>' +
										article.tags_type +
									'</time>' +
								'</li>' +
							'</ul>' +
							'<p class="card-content-description articles-and-blogs">' + '<i>' + article.date + " - " + '</i>' + article.content + '</p>' +
							'<ul class="tags">' +
								tags_year + tags_month + tags_type +
							'</ul>' +
						'</div>' +
					'</article>' +
				'</li>'
		return html;
	};


	// Create HTML for Pagination Buttons
	var setupPagination = function (items, wrapper, items_per_page, ){
		wrapper.innerHTML = "";

		let page_count = Math.ceil(items.length / items_per_page);

		// Page buttons
		for (let i = Math.max(1, current_Page - 2); i <= Math.min(page_count, current_Page + 2); i++){
			let btns = PaginationButton(i, items);
			wrapper.appendChild(btns);
		}

		if(current_Page > 1){
			// First button
			let first_button = document.createElement('button');
			first_button.innerText = "First";
			first_button.removeEventListener('click', ()=>{});
			first_button.addEventListener('click', function (){
				current_Page = 1
				// Scroll to top
				document.body.scrollTop = 0; // For Safari
				document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
				displayList(items, resultList, items_per_page, current_Page);
				// Find button with active class
				let current_btn = document.querySelector('#pagination button.active');
				current_btn.classList.remove('active');
				document.querySelector('#pagination button[id="' + current_Page + '"]').classList.add('active')
			});
			wrapper.insertBefore(first_button, wrapper.firstChild);

			// Previous button
			let previous_button = document.createElement('button');
			previous_button.innerText = "Prev";
			previous_button.removeEventListener('click', ()=>{});
			previous_button.addEventListener('click', function (){
				if (current_Page !=1){
				// Find button with active class, and go to previous page
				let current_btn = document.querySelector('#pagination button.active');
				current_Page = parseInt(current_btn.getAttribute('id')) - 1;
				// Scroll to top
				document.body.scrollTop = 0; // For Safari
				document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
				displayList(items, resultList, items_per_page, current_Page);
				current_btn.classList.remove('active');
				document.querySelector('#pagination button[id="' + current_Page + '"]').classList.add('active')
				}
			});
			wrapper.insertBefore(previous_button, wrapper.firstChild.nextSibling);
		}

		if(current_Page < page_count){
			// Next button
			let next_button = document.createElement('button');
			next_button.innerText = "Next";
			next_button.removeEventListener('click', ()=>{});
			next_button.addEventListener('click', function (){
				if (current_Page != page_count){
				// Find button with active class, and go to next page
				let current_btn = document.querySelector('#pagination button.active');
				current_Page = parseInt(current_btn.getAttribute('id')) + 1;
				// Scroll to top
				document.body.scrollTop = 0; // For Safari
				document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
				displayList(items, resultList, items_per_page, current_Page);
				current_btn.classList.remove('active');
				document.querySelector('#pagination button[id="' + current_Page + '"]').classList.add('active')
				}
			});
			wrapper.appendChild(next_button);

			// Last button
			let last_button = document.createElement('button');
			last_button.innerText = "Last";
			last_button.removeEventListener('click', ()=>{});
			last_button.addEventListener('click', function (){
				current_Page = page_count;
				// Scroll to top
				document.body.scrollTop = 0; // For Safari
				document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
				displayList(items, resultList, items_per_page, current_Page);
				// Find button with active class
				let current_btn = document.querySelector('#pagination button.active');
				current_btn.classList.remove('active');
				document.querySelector('#pagination button[id="' + current_Page + '"]').classList.add('active');
			});
			wrapper.appendChild(last_button);
		}
	}

	// Create Pagination Buttons
	var PaginationButton = function (page, items){
		let button = document.createElement('button');
		button.innerText = page;
		button.setAttribute("id", page);
		if(current_Page == page) button.classList.add('active');

		button.removeEventListener('click', ()=>{});
		button.addEventListener('click', function (){
			current_Page = page;
			// Scroll to top
			document.body.scrollTop = 0; // For Safari
			document.documentElement.scrollTop = 0; // For Chrome, Firefox, IE and Opera
			displayList(items, resultList, items_per_page, current_Page);
		});

		return button;
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

	// Load correct page state on new PageLoad
	var pageLoad = function (){
		let count = 0;
		let count2 = 0;
		let count3 = 0;
		filterItemsContainer.innerHTML = "";
		topInput.value = sessionStorage.getItem("topInput");

		// Check relevant items
		if (sessionStorage.getItem("tagArray")) {
			yearValues.forEach(function (yearValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray")).includes(yearValue)){
					yearItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		if (sessionStorage.getItem("tagArray2")) {
			monthValues.forEach(function (monthValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray2")).includes(monthValue)){
					monthItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		if (sessionStorage.getItem("tagArray3")) {
			typeValues.forEach(function (typeValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray3")).includes(typeValue)){
					typeItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		// Also, create Year Tags
		while (checkedItemArray.length > 0) {
			checkedItemArray.shift();
			}
		yearValues.forEach(function (yearValue, i) {
			if(yearItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-year"' + ' id="'+ yearNames[i] + '"' + '>' + 
					'<span>' +
                        yearValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray.push(yearValue);
			}
		});

		// Also, create Month Tags
		while (checkedItemArray2.length > 0) {
			checkedItemArray2.shift();
			}
		monthValues.forEach(function (monthValue, i) {
			if(monthItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-month"' + ' id="'+ monthNames[i] + '"' + '>' + 
					'<span>' +
                        monthValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray2.push(monthValue);
			}
		});

		// Also, create Type Tags
		while (checkedItemArray3.length > 0) {
			checkedItemArray3.shift();
			}
		typeValues.forEach(function (typeValue, i) {
			if(typeItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-type"' + ' id="'+ typeNames[i] + '"' + '>' + 
					'<span>' +
                        typeValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray3.push(typeValue);
			}
		});
	
		// Also, add Event Listeners to Year Tags
		yearValues.forEach(function (yearValue, i) {
			if(yearItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + yearNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + yearNames[i]).addEventListener('click', TagClick(yearNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Month Tags
		monthValues.forEach(function (monthValue, i) {
			if(monthItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + monthNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + monthNames[i]).addEventListener('click', TagClick2(monthNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Type Tags
		typeValues.forEach(function (typeValue, i) {
			if(typeItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + typeNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + typeNames[i]).addEventListener('click', TagClick3(typeNames[i], i), false);
			}
		});

		// Also, if at least one item is checked, show 'filter clear' button(s)
		countTotalCheckedItems();

		// Do search
		if (sessionStorage.getItem("page")) {
			search(topInput.value, 	sessionStorage.getItem("page"));
		} else {
			search(topInput.value, 1);
		}

	}

	// Do the following on Page Load

	// Make sure required content exists
	if (!topForm || !topInput || !resultList || !searchIndex) return;

	// Remove site: from the input
	clearInput();

	// Display all session items on page load
	pageLoad();

	// Add Event Listerns
	eventListeners();

    function eventListeners() {
		// Run seach everytime Top 'Search' form is submitted
		topForm.addEventListener('submit', submitHandler, false);

		// Count number of Year checked items, on each state change
		yearValues.forEach(function (yearValue, i) {
			yearItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
		});

		// Count number of Month checked items, on each state change
		monthValues.forEach(function (monthValue, i) {
			monthItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
		});

		// Count number of Type checked items, on each state change
		typeValues.forEach(function (typeValue, i) {
			typeItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
		});

		// Change logic switch
		logicSwitch.addEventListener('change', changeLogicSwitch);

		// Uncheck every checkbox if 'filter clear' button is clicked
		filterClear.addEventListener('click', clearFilter);
		filterClear2.addEventListener('click', clearFilter2);
		filterClear3.addEventListener('click', clearFilter3);
		filterClear4.addEventListener('click', clearFilter4);
	}

})();

// WAIT TILL DOCUMENT HAS LOADED BEFORE INITIATING FUNCTIONS
let searchAandB;
document.addEventListener('DOMContentLoaded', searchAandB);

