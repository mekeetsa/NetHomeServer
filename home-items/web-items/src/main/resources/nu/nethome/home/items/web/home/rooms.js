/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

function gotoRoomEditPage() {
    newUrl = homeManager.baseURL + "?page=edit&a=create&mode=edit&return=rooms&returnsp=" + homeManager.location + "&class_name=Room&room=" + homeManager.location;
    location.href=newUrl;
}

function callItemAction(item, action) {
    var url = homeManager.baseURL + "?a=ajax&name=" + escape(item) + "&action=" + escape(action);
    $.get(url, getItemValues);
}

function getItemValues() {
    var valueElements = $(".attrvalue").toArray();
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
    var attributeValueElements = $(".attrvalue").toArray();
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
            iconClassForValue = $(icons[i]).attr("data-" + itemMainAttributeValues[itemId]);
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
}

