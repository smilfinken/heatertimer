(function (window) {
    function incrementValue(id, delta, min, max)
    {
        if (min < max && delta < max - min) {
            var value = parseInt(document.getElementById(id).value, 0);
            value = isNaN(value) ? 0 : value += delta;
            if (value > max) {
                value -= (max + 1);
            } else {
            }
            document.getElementById(id).value = value;
        }
    }

    function decrementValue(id, delta, min, max)
    {
        if (min < max && delta < max - min) {
            var value = parseInt(document.getElementById(id).value, 0);
            value = isNaN(value) ? 0 : value -= delta;
            if (value < min) {
                value += (max + 1);
            } else {
            }
            document.getElementById(id).value = value;
        }
    }

    function highlightRow(id) {
        var row = document.getElementById(id);
        classie.toggle(row, "highlighted");
    }

	window.utils = {
		incrementValue: incrementValue,
		decrementValue: decrementValue,
		highlightRow: highlightRow
	};
})(window);
