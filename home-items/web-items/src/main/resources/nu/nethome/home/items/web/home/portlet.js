
function callItemAction(item, action) {
    var url = homeManager.baseURL + "?a=ajax&name=" + escape(item) + "&action=" + escape(action);
    $.get(url, getItemValues);
}

function getItemValues() {
    var valueElements = $(".itemvalue").toArray();
    var parameter = "";
    var separator = "";
    var singleGuard = {};
    var id;
    var url = homeManager.baseURL + "?a=ajax&f=getdefatts&items="
    for (i = 0; i < valueElements.length; i++) {
        id = $(valueElements[i]).attr("data-item");
        if (!singleGuard[id]) {
            parameter = parameter + separator + id;
            separator = "-";
            singleGuard[id] = true;
        }
    }
    url = url + parameter;
    $.getJSON(url, updateItemValues);
}

function updateItemValues(itemMainAttributeValues) {
    var attributeValueElements = $(".itemvalue").toArray();
    var itemId;
    var i;
    var iconClassForValue;
    var lastIconClass;
    var attributeValue;

    for (i = 0; i < attributeValueElements.length; i++) {
        itemId = $(attributeValueElements[i]).attr("data-item");
        if (itemMainAttributeValues[itemId]) {
            attributeValue = itemMainAttributeValues[itemId];
            if ($(attributeValueElements[i]).attr("data-unit")) {
                attributeValue += " " + $(attributeValueElements[i]).attr("data-unit");
            }
            attributeValueElements[i].innerHTML = attributeValue;
        }
    }
    var icons = $(".icon").toArray();
    for (i = 0; i < icons.length; i++) {
        itemId = $(icons[i]).attr("data-item");
        if (itemId && itemMainAttributeValues[itemId]) {
            if (  itemMainAttributeValues[itemId].substr(0,3) == "On " ) {
                iconClassForValue = $(icons[i]).attr("data-On");
            } else {
                iconClassForValue = $(icons[i]).attr("data-" + itemMainAttributeValues[itemId]);
            }
            lastIconClass = $(icons[i]).attr("data-lastclass");
            if (lastIconClass) {
                $(icons[i]).removeClass(lastIconClass);
                $(icons[i]).removeAttr("data-lastclass");
            }
            if (iconClassForValue) {
                $(icons[i]).addClass(iconClassForValue);
                $(icons[i]).attr("data-lastclass", iconClassForValue);
            }
        }
    }

    // handle data-items inside SVG
    if ( svgObject !== null ) {
        attributeValueElements = svgObject.getElementsByClassName("itemvalue");
        for (i = 0; i < attributeValueElements.length; i++) {
            itemId = $(attributeValueElements[i]).attr("data-item");
            if (itemMainAttributeValues[itemId]) {
                attributeValue = itemMainAttributeValues[itemId];
                if ($(attributeValueElements[i]).attr("data-unit")) {
                    attributeValue += " " + $(attributeValueElements[i]).attr("data-unit");
                }
                attributeValueElements[i].innerHTML = attributeValue;
            }
        }
    }
}

$(document).ready(function () {
    if (typeof portletUpdateInterval === 'undefined' || portletUpdateInterval === null) {
        portletUpdateInterval = 5000; // default 5 seconds
    }
    setInterval(getItemValues,portletUpdateInterval);
    // Ikea nodes satus update
    // TODO: make this configurable (e.g., do it only if there are Ikea items in the list)
    callItemAction('133', 'reportNodes');
});
