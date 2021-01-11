// Find parent with given class
function parent_by_class(elem, cls, stop_selector = 'body') {
    return elem.closest("." + cls)
}

// Find parent with given selector
function parent_by_selector(elem, selector, stop_selector = 'body') {
    return elem.closest(selector)
}

// Find child with given selector
function child_by_selector(elem, selector) {
    let children = elem.childNodes;
    for (let i = 0; i < children.length; i++) {
        if (children[i].className &&
            children[i].className.split(' ').indexOf(selector) >= 0) {
            return children[i];
         }
     }
     for (let i = 0; i < children.length; i++) {
         let child = child_by_selector(children[i], selector);
         if (child !== null) {
             return child;
         }
     }
     return null;
}

// Find next sibling of particular class
function nextByClass(elem, cls) {
    while (elem = elem.nextElementSibling) {
        if (hasClass(elem, cls)) {
            return elem;
        }
    }
    return null;
}

// Find previous sibling of particular class
function previousByClass(elem, cls) {
    while (elem = elem.previousElementSibling) {
        if (hasClass(elem, cls)) {
            return elem;
        }
    }
    return null;
}

// Sibling class found?
function hasClass(elem, cls) {
    return elem.classList.contains(cls);
}

(function Modal () {
    // Get the modal
    var modal = document.getElementById("myModal");

    // Get the button that opens the modal
    var btn = document.getElementById("modalBtn");

    // Get the <span> element that closes the modal
    var span = document.getElementsByClassName("close")[0];

    // When the user clicks the button, open the modal 
    if (btn != null) {
        btn.onclick = function() {
        modal.style.display = "block";
        }
    }

    // When the user clicks on <span> (x), close the modal
    if (span != null) {
        span.onclick = function() {
        modal.style.display = "none";
        }
    }

    // When the user clicks anywhere outside of the modal, close it
    window.onclick = function(event) {
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }
}) ();

function showImage (imgName) {
    // Get the modal
    var modal = document.getElementById("myModal");
    var modalbody = document.querySelector("#myModal .modal-body");
    modalbody.innerHTML = 
    '<figure>' +
    '<img class="docimage" ' +
    'src="images/' + imgName +'" alt="" style="width: 100%; max-width: " />' +
    '<figcaption style="text-align: center; color: #5e5e5e; font-size: 0.9rem;">' +
        '<i></i>' +
    '</figcaption>' +
    '</figure>';
    modal.style.display = "block";
};



