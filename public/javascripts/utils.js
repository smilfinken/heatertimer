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

    function highlightSingleRow(id) {
        var row = document.getElementById(id);
        if (!!row) {
            var rows = row.parentNode.getElementsByTagName("tr");
            for (var i = 0; i < rows.length; i++) {
                if (rows[i].id === id) {
                    if (!classie.has(rows[i], "highlighted")) {
                        classie.add(rows[i], "highlighted");
                    }
                } else {
                    if (classie.has(rows[i], "highlighted")) {
                        classie.remove(rows[i], "highlighted");
                    }
                }
            }
            return true;
        }
    }

    function highlightRow(id) {
        var row = document.getElementById(id);
        if (!!row) {
            classie.toggle(row, "highlighted");
        }
    }

    function setValue(target, attribute, value) {
        var element = document.getElementById(target);
        if (!!element) {
            element.setAttribute(attribute, value);
            return true;
        }
    }

	window.utils = {
		incrementValue: incrementValue,
		decrementValue: decrementValue,
		highlightSingleRow: highlightSingleRow,
		highlightRow: highlightRow,
		setValue: setValue
	};
})(window);
