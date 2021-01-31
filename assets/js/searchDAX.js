
"use strict";

(function searchDAX() {

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


	const filterInput = document.querySelector('#filter-search');
	const filterInput2 = document.querySelector('#filter-search-2');
	const filterInput3 = document.querySelector('#filter-search-3');
	const measureItems = [...document.querySelectorAll(".measure-item")];
	const nonMeasureItems = [...document.querySelectorAll(".non-measure-item")];
	const daxItems = [...document.querySelectorAll(".dax-item")];
	const filterItemsContainer = document.querySelector("#filter-items-container");
	const filterClear = document.querySelector('#filter-clear');
	const filterClear2 = document.querySelector('#filter-clear-2');
	const filterClear3 = document.querySelector('#filter-clear-3');
	const filterClear4 = document.querySelector('#filter-clear-4');

	// put all Measure Items into a single Array
	let measureValues = [];
	let measureNames =[];
	for (let i = 0; i < measureItems.length; i++) {
		measureValues.push(measureItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		measureNames.push(measureItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray = [];
	if (sessionStorage.getItem("tagArray")) {
		// Get checked items from Session Storage
		checkedItemArray = JSON.parse(sessionStorage.getItem("tagArray"));
	  }

	// put all Non-Measure Items into a single Array
	let nonMeasureValues = [];
	let nonMeasureNames =[];
	for (let i = 0; i < nonMeasureItems.length; i++) {
		nonMeasureValues.push(nonMeasureItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		nonMeasureNames.push(nonMeasureItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray2 = [];
	if (sessionStorage.getItem("tagArray2")) {
		// Get checked items from Session Storage
		checkedItemArray2 = JSON.parse(sessionStorage.getItem("tagArray2"));
	  }

	// put all DAX Items into a single Array
	let daxValues = [];
	let daxNames =[];
	for (let i = 0; i < daxItems.length; i++) {
		daxValues.push(daxItems[i].firstElementChild.firstElementChild.getAttribute('value'));
		daxNames.push(daxItems[i].firstElementChild.firstElementChild.getAttribute('name'));
	}
	let checkedItemArray3 = [];
	if (sessionStorage.getItem("tagArray3")) {
		// Get checked items from Session Storage
		checkedItemArray3 = JSON.parse(sessionStorage.getItem("tagArray3"));
	  }

	// Filter side Menu 1
	var filterMeasureMenu = function (event) {
		event.preventDefault();
		let filterInputValue = filterInput.value ? filterInput.value : null;

		// Do filtering if filter string is not empty
		if(filterInput.value.trim()){
			let count = 0;
			measureValues.forEach(function (measureValue, i) {
				// Convert Input String to Array of Words
				let filterArray = filterInputValue.match(/\b(\w+)\b/g);
				if(matchWordsPartial(measureValue, filterArray).length > 0){
					measureItems[i].classList.remove('is-hidden')
					count++
				} else{
					measureItems[i].classList.add('is-hidden')
				};
			});
			// Show No Results paragraph, if no results are found
			if (count == 0) {
				document.querySelector('#no-results').classList.remove('is-hidden');
			} else{
				document.querySelector('#no-results').classList.add('is-hidden');
			}
		} else{
			measureValues.forEach(function (measureValue, i) {
				measureItems[i].classList.remove('is-hidden');
			});
		}
	
	}


	// Filter side Menu 2
	var filterMeasureMenu2 = function (event) {
		event.preventDefault();
		let filterInputValue2 = filterInput2.value ? filterInput2.value : null;

		// Do filtering if filter string is not empty
		if(filterInput2.value.trim()){
			let count = 0;
			nonMeasureValues.forEach(function (nonMeasureValue, i) {
				// Convert Input String to Array of Words
				let filterArray = filterInputValue2.match(/\b(\w+)\b/g);
				if(matchWordsPartial(nonMeasureValue, filterArray).length > 0){
					nonMeasureItems[i].classList.remove('is-hidden')
					count++
				} else{
					nonMeasureItems[i].classList.add('is-hidden')
				};
			});
			// Show No Results paragraph, if no results are found
			if (count == 0) {
				document.querySelector('#no-results-2').classList.remove('is-hidden');
			} else{
				document.querySelector('#no-results-2').classList.add('is-hidden');
			}
		} else{
			nonMeasureValues.forEach(function (nonMeasureValue, i) {
				nonMeasureItems[i].classList.remove('is-hidden');
			});
		}
	
	}

	// Filter side Menu 3
	var filterMeasureMenu3 = function (event) {
		event.preventDefault();
		let filterInputValue3 = filterInput3.value ? filterInput3.value : null;

		// Do filtering if filter string is not empty
		if(filterInput3.value.trim()){
			let count = 0;
			daxValues.forEach(function (daxValue, i) {
				// Convert Input String to Array of Words
				let filterArray = filterInputValue3.match(/\b(\w+)\b/g);
				if(matchWordsPartial(daxValue, filterArray).length > 0){
					daxItems[i].classList.remove('is-hidden')
					count++
				} else{
					daxItems[i].classList.add('is-hidden')
				};
			});
			// Show No Results paragraph, if no results are found
			if (count == 0) {
				document.querySelector('#no-results-3').classList.remove('is-hidden');
			} else{
				document.querySelector('#no-results-3').classList.add('is-hidden');
			}
		} else{
			daxValues.forEach(function (daxValue, i) {
				daxItems[i].classList.remove('is-hidden');
			});
		}
	
	}

	// Count total number of checked items
	var countTotalCheckedItems = function (){
		let count = 0;
		let count2 = 0;
		let count3 = 0;

		// Count Measure Items
		measureValues.forEach(function (measureValue, i) {
			if(measureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
			}
		});

		// Count Non-Measure Items
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
			}
		});

		// Count DAX Items
		daxValues.forEach(function (daxValue, i) {
			if(daxItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
			}
		});

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

		// Also, create Measure Tags
		measureValues.forEach(function (measureValue, i) {
			if(measureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-m"' + ' id="'+ measureNames[i] + '"' + '>' + 
					'<span>' +
						measureValue +
					'</span>' +
					'<span class="delete">' +
                    '</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray.push(measureValue);
			}
		});
		sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));

		// Also, create Non-Measure Tags
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-d"' + ' id="'+ nonMeasureNames[i] + '"' + '>' + 
					'<span>' +
						nonMeasureValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray2.push(nonMeasureValue);
			}
		});
		sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));

		// Also, create DAX Tags
		daxValues.forEach(function (daxValue, i) {
			if(daxItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-dax"' + ' id="'+ daxNames[i] + '"' + '>' + 
					'<span>' +
						daxValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray3.push(daxValue);
			}
		});
		sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
		
		// Also, add Event Listeners to Measure Tags
		measureValues.forEach(function (measureValue, i) {
			if(measureItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + measureNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + measureNames[i]).addEventListener('click', TagClick(measureNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Non-Measure Tags
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + nonMeasureNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + nonMeasureNames[i]).addEventListener('click', TagClick2(nonMeasureNames[i], i), false);
			}
		});

		// Also, add Event Listeners to DAX Tags
		daxValues.forEach(function (daxValue, i) {
			if(daxItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + daxNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + daxNames[i]).addEventListener('click', TagClick3(daxNames[i], i), false);
			}
		});

		// If at least one item is checked, and filter input is empty, hide 'filter input bar'
		if(count > 0 && !filterInput.value.trim()){
			filterInput.classList.add('is-hidden');
		} else {
			filterInput.classList.remove('is-hidden');
		}

		// If at least one item is checked, and filter input is empty, hide 'filter input bar 2'
		if(count2 > 0 && !filterInput2.value.trim()){
			filterInput2.classList.add('is-hidden');
		} else {
			filterInput2.classList.remove('is-hidden');
		}

		// If at least one item is checked, and filter input is empty, hide 'filter input bar 3'
		if(count3 > 0 && !filterInput3.value.trim()){
			filterInput3.classList.add('is-hidden');
		} else {
			filterInput3.classList.remove('is-hidden');
		}

		// Also, if at least one item is checked, show 'filter clear' button
		countTotalCheckedItems();

		// Do search
		search(topInput.value, 1);
	}

	// Uncheck Checkboxes, and remove Measure Tag, when Tag is clicked
	var TagClick = function (tag, i){
		
		return function (){
			let count = 0;
			while (checkedItemArray.length > 0) {
				checkedItemArray.shift();
			}

			// Uncheck relevant CheckBox 
			measureItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items
			measureValues.forEach(function (measureValue, i) {
				if(measureItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray.push(measureValue);
				}
			});
			sessionStorage.setItem("tagArray", JSON.stringify(checkedItemArray));
			current_Page = 1;
			
			// If at least one item is checked, and filter input is empty, hide 'filter input bar'
			if(count > 0 && !filterInput.value.trim()){
				filterInput.classList.add('is-hidden');
			} else {
				filterInput.classList.remove('is-hidden');
			}

			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};

	// Uncheck Checkboxes, and remove Non-Measure Tag, when Tag is clicked
	var TagClick2 = function (tag, i){
	
		return function (){
			let count = 0;

			while (checkedItemArray2.length > 0) {
				checkedItemArray2.shift();
			}

			// Uncheck relevant CheckBox 
			nonMeasureItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items

			nonMeasureValues.forEach(function (nonMeasureValue, i) {
				if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray2.push(nonMeasureValue);
				}
			});
			sessionStorage.setItem("tagArray2", JSON.stringify(checkedItemArray2));
			current_Page = 1;

			// If at least one item is checked, and filter input is empty, hide 'filter input bar 2'
			if(count > 0 && !filterInput2.value.trim()){
				filterInput2.classList.add('is-hidden');
			} else {
				filterInput2.classList.remove('is-hidden');
			}

			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};

	// Uncheck Checkboxes, and remove DAX Tag, when Tag is clicked
	var TagClick3 = function (tag, i){
	
		return function (){
			let count = 0;

			while (checkedItemArray3.length > 0) {
				checkedItemArray3.shift();
			}

			// Uncheck relevant CheckBox 
			daxItems[i].firstElementChild.firstElementChild.checked = false;

			// Remove Tag
			filterItemsContainer.querySelector("#" + tag).remove();

			// Recount Checked Items

			daxValues.forEach(function (daxValue, i) {
				if(daxItems[i].firstElementChild.firstElementChild.checked){
					count++;
				// Update checked item array
				checkedItemArray3.push(daxValue);
				}
			});
			sessionStorage.setItem("tagArray3", JSON.stringify(checkedItemArray3));
			current_Page = 1;

			// If at least one item is checked, and filter input is empty, hide 'filter input bar 2'
			if(count > 0 && !filterInput3.value.trim()){
				filterInput3.classList.add('is-hidden');
			} else {
				filterInput3.classList.remove('is-hidden');
			}

			// Also, if at least one item is checked, show the 'filter clear' button
			countTotalCheckedItems();

			// Do search
			search(topInput.value, 1);
		}
	};	


	// Clear all Filters
	var clearFilter = function (){
		measureValues.forEach(function (currentValue , i) {
			measureItems[i].classList.remove('is-hidden')
			measureItems[i].firstElementChild.firstElementChild.checked = false;
		});

		nonMeasureValues.forEach(function (currentValue, i) {
			nonMeasureItems[i].classList.remove('is-hidden')
			nonMeasureItems[i].firstElementChild.firstElementChild.checked = false;
		});

		daxValues.forEach(function (currentValue, i) {
			daxItems[i].classList.remove('is-hidden')
			daxItems[i].firstElementChild.firstElementChild.checked = false;
		});

		filterClear.classList.add('is-hidden');
		document.querySelector('#no-results').classList.add('is-hidden');
		document.querySelector('#no-results-2').classList.add('is-hidden');
		document.querySelector('#no-results-3').classList.add('is-hidden');

		filterInput.classList.remove('is-hidden');
		filterInput.value = '';
		filterInput2.classList.remove('is-hidden');
		filterInput2.value = '';
		filterInput3.classList.remove('is-hidden');
		filterInput3.value = '';

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

	// Clear all Measure Filters
	var clearFilter2 = function (){
		measureValues.forEach(function (measureValue, i) {
			measureItems[i].classList.remove('is-hidden')
			measureItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#no-results').classList.add('is-hidden');
		filterInput.classList.remove('is-hidden');
		filterInput.value = '';

		// Remove Measure Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-m')		
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

	// Clear all Non-Measure Filters
	var clearFilter3 = function (){
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			nonMeasureItems[i].classList.remove('is-hidden')
			nonMeasureItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#no-results-2').classList.add('is-hidden');
		filterInput2.classList.remove('is-hidden');
		filterInput2.value = '';

		// Remove Non-Measure Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-d')		
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

	// Clear all DAX Filters
	var clearFilter4 = function (){
		daxValues.forEach(function (daxValue, i) {
			daxItems[i].classList.remove('is-hidden')
			daxItems[i].firstElementChild.firstElementChild.checked = false;
		});
		document.querySelector('#no-results-3').classList.add('is-hidden');
		filterInput3.classList.remove('is-hidden');
		filterInput3.value = '';

		// Remove DAX Tags
		let checkedItems = filterItemsContainer.getElementsByClassName('tag-dax')		
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
		var priority0 = []; // for search in measure, dimension, and dax tag fields
		var priority1 = []; // for search in title & content fields
		var results = [];

		// Search Measure, Dimension, & DAX Tags
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
					for (let i=0; i < article.tags_measure.length; i++) {
						tagArray.forEach(function (tag){
							if(article.tags_measure[i] == tag) match1 = true;
						});
					}
					for (let i=0; i < article.tags_dimension.length; i++) {
						tagArray2.forEach(function (tag){
							if(article.tags_dimension[i] == tag) match2 = true;
						});
					}
					for (let i=0; i < article.tags_dax.length; i++) {
						tagArray3.forEach(function (tag){
							if(article.tags_dax[i] == tag) match3 = true;
						});
					}
					if (match1 && match2 && match3) priority0.push(article);
				} else {
					for (let i=0; i < article.tags_measure.length; i++) {
						tagArray.forEach(function (tag){
							if(article.tags_measure[i] == tag) match1 = true;
						});
					}
					for (let i=0; i < article.tags_dimension.length; i++) {
						tagArray2.forEach(function (tag){
							if(article.tags_dimension[i] == tag) match2 = true;
						});
					}
					for (let i=0; i < article.tags_dax.length; i++) {
						tagArray3.forEach(function (tag){
							if(article.tags_dax[i] == tag) match3 = true;
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

		// Add Article Counts by Measures to Page
			// Show Measures for all Matched Articles
			let measureArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_measure.length; i++) {
					measureValues.forEach(function (measure){
						if(article.tags_measure[i] == measure) measureArray.push(article.tags_measure[i]);
					});
				}
			});

			// Show Article Count Grouped by Measure
			let measureObjectCount = measureArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from Meaure Items in HTML
			measureValues.forEach(function (currentValue, i) {
				measureItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all Measure Items in HTML
			Object.keys(measureObjectCount).forEach(function(key) {
				let measureElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-m";
				let measureElement = document.getElementById(measureElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				measureElement.innerHTML = "&nbsp (" + measureObjectCount[key] + ")";
			});


		// Add Article Counts by Non-Measures to Page
			// Show Non-Measures for all Matched Articles
			let nonMeasureArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_dimension.length; i++) {
					nonMeasureValues.forEach(function (dimension){
						if(article.tags_dimension[i] == dimension) nonMeasureArray.push(article.tags_dimension[i]);
					});
				}
			});

			// Show Article Count Grouped by Non-Measure
			let nonMeasureObjectCount = nonMeasureArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from Non-Meaure Items in HTML
			nonMeasureValues.forEach(function (currentValue, i) {
				nonMeasureItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all Non-Measure Items in HTML
			Object.keys(nonMeasureObjectCount).forEach(function(key) {
				let nonMeasureElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-nm";
				let nonMeasureElement = document.getElementById(nonMeasureElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				nonMeasureElement.innerHTML = "&nbsp (" + nonMeasureObjectCount[key] + ")";
			});


		// Add Article Counts by DAX to Page
			// Show DAX for all Matched Articles
			let daxArray = [];

			results.forEach(function (article) {
				for (let i=0; i < article.tags_dax.length; i++) {
					daxValues.forEach(function (dax){
						if(article.tags_dax[i] == dax) daxArray.push(article.tags_dax[i]);
					});
				}
			});

			// Show Article Count Grouped by DAX
			let daxObjectCount = daxArray.reduce((r,c) => (r[c] = (r[c] || 0) + 1, r), {});

			// Clear all Counts from DAX Items in HTML
			daxValues.forEach(function (currentValue, i) {
				daxItems[i].firstElementChild.firstElementChild.nextElementSibling.nextElementSibling.nextElementSibling.innerHTML = "";
			});
			
			// Add Updated Counts for all DAX Items in HTML
			Object.keys(daxObjectCount).forEach(function(key) {
				let daxElementId = "filter-" + key.replace(/\s+/g, '-').toLowerCase() + "-dax";
				let daxElement = document.getElementById(daxElementId).nextElementSibling.nextElementSibling.nextElementSibling;
				daxElement.innerHTML = "&nbsp (" + daxObjectCount[key] + ")";
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
						'<h2 class="title is-6">'
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
		var tags_measure = "";
		for (let i=0; i < article.tags_measure.length; i++) {
		tags_measure += '<li class="tag-m is-small">' + article.tags_measure[i] + '</li>';
		};

		var tags_dimension = "";
		for (let i=0; i < article.tags_dimension.length; i++) {
		tags_dimension += '<li class="tag-d is-small">' + article.tags_dimension[i] + '</li>';
		};

		var tags_dax = "";
		for (let i=0; i < article.tags_dax.length; i++) {
		tags_dax += '<li class="tag-dax is-small">' + article.tags_dax[i] + '</li>';
		};

		var html =
				'<li class="grid-item dax">' +
					'<article class="card dax">' +
						'<div class="card-content">' +
							'<a class="card-content-title dax" href="' + article.url + '">' +
								'<h3>' + article.title + '</h3>' +
							'</a>' +
							'<ul class="card-content-metadata dax">' +
								'<li>' +
									'<time>' +
										article.date +
									'</time>' +
								'</li>' +
							'</ul>' +
							'<p class="card-content-description">' + article.content + '</p>' +
							'<ul class="tags">' +
								tags_measure + tags_dimension + tags_dax +
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
			measureValues.forEach(function (measureValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray")).includes(measureValue)){
					measureItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		if (sessionStorage.getItem("tagArray2")) {
			nonMeasureValues.forEach(function (nonMeasureValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray2")).includes(nonMeasureValue)){
					nonMeasureItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		if (sessionStorage.getItem("tagArray3")) {
			daxValues.forEach(function (daxValue, i) {
				if(JSON.parse(sessionStorage.getItem("tagArray3")).includes(daxValue)){
					daxItems[i].firstElementChild.firstElementChild.checked = true;
				}
			});
		}
		// Also, create Measure Tags
		while (checkedItemArray.length > 0) {
			checkedItemArray.shift();
			}
		measureValues.forEach(function (measureValue, i) {
			if(measureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-m"' + ' id="'+ measureNames[i] + '"' + '>' + 
					'<span>' +
						measureValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray.push(measureValue);
			}
		});

		// Also, create Non-Measure Tags
		while (checkedItemArray2.length > 0) {
			checkedItemArray2.shift();
			}
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count2++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-d"' + ' id="'+ nonMeasureNames[i] + '"' + '>' + 
					'<span>' +
						nonMeasureValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray2.push(nonMeasureValue);
			}
		});

		// Also, create DAX Tags
		while (checkedItemArray3.length > 0) {
			checkedItemArray3.shift();
			}
		daxValues.forEach(function (daxValue, i) {
			if(daxItems[i].firstElementChild.firstElementChild.checked){
				// Count checked items
				count3++;
				// Show Tags of Checked Items
				filterItemsContainer.innerHTML += 
				'<button class="tag-dax"' + ' id="'+ daxNames[i] + '"' + '>' + 
					'<span>' +
						daxValue +
					'</span>' +
					'<span class="delete">' +
					'</span>' +
				'</button>';
				// Update checked item array
				checkedItemArray3.push(daxValue);
			}
		});
	
		// Also, add Event Listeners to Measure Tags
		measureValues.forEach(function (measureValue, i) {
			if(measureItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + measureNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + measureNames[i]).addEventListener('click', TagClick(measureNames[i], i), false);
			}
		});

		// Also, add Event Listeners to Non-Measure Tags
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			if(nonMeasureItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + nonMeasureNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + nonMeasureNames[i]).addEventListener('click', TagClick2(nonMeasureNames[i], i), false);
			}
		});

		// Also, add Event Listeners to DAX Tags
		daxValues.forEach(function (daxValue, i) {
			if(daxItems[i].firstElementChild.firstElementChild.checked){
				// Avoid creating Duplicate Event Listeners
				filterItemsContainer.querySelector("#" + daxNames[i]).removeEventListener('click',()=>{});
				filterItemsContainer.querySelector("#" + daxNames[i]).addEventListener('click', TagClick3(daxNames[i], i), false);
			}
		});

		// If at least one item is checked, and filter input is empty, hide 'filter input bar'
		if(count > 0 && !filterInput.value.trim()){
			filterInput.classList.add('is-hidden');
		} else {
			filterInput.classList.remove('is-hidden');
		}

		// If at least one item is checked, and filter input is empty, hide 'filter input bar 2'
		if(count2 > 0 && !filterInput2.value.trim()){
			filterInput2.classList.add('is-hidden');
		} else {
			filterInput2.classList.remove('is-hidden');
		}

		// If at least one item is checked, and filter input is empty, hide 'filter input bar 3'
		if(count3 > 0 && !filterInput3.value.trim()){
			filterInput3.classList.add('is-hidden');
		} else {
			filterInput3.classList.remove('is-hidden');
		}	

		// Also, if at least one item is checked, show 'filter clear' button
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

		// Filter Measure side menu everytime there is a key-up event
		filterInput.addEventListener('keyup', filterMeasureMenu);

		// Filter Non-Measure side menu everytime there is a key-up event
		filterInput2.addEventListener('keyup', filterMeasureMenu2);

		// Filter DAX side menu everytime there is a key-up event
		filterInput3.addEventListener('keyup', filterMeasureMenu3);

		// Count number of Measure checked items, on each state change
		measureValues.forEach(function (measureValue, i) {
			measureItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
		});

		// Count number of Non-Measure checked items, on each state change
		nonMeasureValues.forEach(function (nonMeasureValue, i) {
			nonMeasureItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
		});

		// Count number of DAX checked items, on each state change
		daxValues.forEach(function (daxValue, i) {
			daxItems[i].firstElementChild.firstElementChild.addEventListener('change', countCheckedItems);
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
let searchDAX;
document.addEventListener('DOMContentLoaded', searchDAX);

