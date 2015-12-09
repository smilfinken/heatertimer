(function (window) {
	function enableButton(element) {
		var button = document.getElementById(element);
        if (!!button && classie.has(button, "disabled")) {
            classie.remove(button, "disabled");
        }
	}

	function disableButton(element) {
		var button = document.getElementById(element);
        if (!!button && !classie.has(button, "disabled")) {
            classie.add(button, "disabled");
        }
	}

    function connectButton(element, target) {
		var button = document.getElementById(element);
		if (!!button) {
            button.setAttribute("form", target);
            enableButton(button.id);
		}
    }

    function disconnectButton(element, target) {
		var button = document.getElementById(element);
		if (!!button) {
            button.removeAttribute("form");
            disableButton(button.id);
		}
    }

	window.toolbar = {
		enableButton: enableButton,
		disableButton: disableButton,
		connectButton: connectButton,
		disconnectButton: disconnectButton
	};
})(window);
