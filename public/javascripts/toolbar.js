(function (window) {
	function disableButton(element) {
		var button = document.getElementById(element);
        classie.add(button, "disabled");
	}

	function enableButton(element) {
		var button = document.getElementById(element);
        classie.remove(button, "disabled");
	}

    function connectButton(element, target) {
		var button = document.getElementById(element);
		button.setAttribute("form", target);
    }

	window.toolbar = {
		enableButton: enableButton,
		connectButton: connectButton
	};
})(window);
