(function (window) {
	function attachEvents() {
		var menuLeft = document.getElementById( 'cbp-spmenu-s1' );
		var showMenu = document.getElementById( 'showMenu' );
		var body = document.body;

		showMenu.onclick = function() {
			classie.toggle( menuLeft, 'cbp-spmenu-open' );
		};

		menuLeft.onclick = function() {
			classie.toggle( menuLeft, 'cbp-spmenu-open' );
		};
	}

	window.menu = {
		attachEvents: attachEvents
	};
})(window);
