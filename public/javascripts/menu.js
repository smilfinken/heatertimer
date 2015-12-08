(function (window) {
	function attachEvents() {
		var menuLeft = document.getElementById('menu');
		var showMenu = document.getElementById('showmenu');

		showMenu.onclick = function() {
			classie.toggle(menuLeft, 'open');
			classie.toggle(showMenu, 'open');
		};

		menuLeft.onclick = function() {
			classie.toggle(menuLeft, 'open');
			classie.toggle(showMenu, 'open');
		};
	}

	window.menu = {
		attachEvents: attachEvents
	};
})(window);
