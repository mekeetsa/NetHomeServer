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

function rescalePlan() {
    // Scale #plan to fit #pageBody
    var pagew = $("#pageBody").width();
    var planw = $("#plan").width();
    if ( pagew > 900 ) { pagew = 900; }
    var scale = pagew / planw;
    var shift = ( pagew - planw ) / 2;
    $("#plan").css("transform","translateX(" + shift + "px) scale(" + scale + ")" );
}

$(window).resize(rescalePlan);

var svgObject = null;

$(document).ready(function () {
    $(".phomeitem").hide();
    $(".panelclose").click(function () {
        $(".pitemlistpanel").fadeOut();
    });
    $(".draggable").draggable({stop: repositionItem});
    $(".poppable").click(onHover);
    $(".closebutton").click(onClose);

    rescalePlan ();

    // In the case of SVG, set the size #svgDiv according to the size of #svgObject
    // (otherwise sets the min-height of #pageBody).
    if ( $("#svgDiv").length > 0 && $("#svgObject").length > 0 ) {
        $("#svgObject")[0].addEventListener( "load", function () { 
            svgObject = this.getSVGDocument().children[0];
            $("#svgDiv").css("width",  svgObject.width["baseVal"].value+'px');
            $("#svgDiv").css("height", svgObject.height["baseVal"].value+'px');
        }, true );
    } else {
        $("#plan").css("min-height","600px");
    }
});


function showItemSelectionPanel() {
    $(".pitemlistpanel").fadeIn();
}

function onClose(event) {
    var closeButton = event.currentTarget;
    var elementToClose = closeButton.parentElement.parentElement.parentElement;
    $(elementToClose).fadeOut();
}

function onHover(event) {
    var evt = event.currentTarget;
    var itemId = $(event.currentTarget).attr("data-item");
    $(".phomeitem[data-item!='" + itemId + "']").fadeOut();
    var objid = $(".phomeitem[data-item=" + itemId + "]").get(0);
    //get width and height of background map
//    var mapWidth  = evt.parentNode.offsetWidth;
//    var mapHeight = evt.parentNode.offsetHeight;
//    //get width and height of the popup
//    var toopTipWidth = objid.offsetWidth;
//    var toopTipHeight = objid.offsetHeight;
//    //figure out where tooltip should be places based on point location
    var newX = evt.offsetLeft + 65;
    var newY = evt.offsetTop + 25;
//    //check if tooltip fits map width
//    if ((newX + toopTipWidth) > mapWidth) {
//        objid.style.left = newX-toopTipWidth-24 + 'px';
//    } else {
//        objid.style.left = newX + 'px';
//    };
//    //check if tooltip fits map height
//    if ((newY + toopTipHeight) > mapHeight) {
//        objid.style.top = newY-toopTipHeight-14 + 'px';
//    } else {
//        objid.style.top = newY + 'px';
//    };
    objid.style.top = newY + 'px';
    objid.style.left = newX + 'px';
    $(objid).fadeIn();
}

function repositionItem(event, ui) {
    var theItem = event.target;
    var url = homeManager.baseURL + "?a=ajax&f=reposition&item=";
    url = url + $(theItem).attr("data-item");
    url = url + "&plan=" + $(theItem).attr("data-plan");
    url = url + "&x=" + ui.position.left;
    url = url + "&y=" + ui.position.top;
    $.get(url);
}

function gotoPlanEditPage() {
    var planId = $(".plan").attr("data-item");
    var subpage = "";
    if (homeManager.subpage) {
        subpage = "&returnsp=" + homeManager.subpage;
    }
    location.href = homeManager.baseURL + "?page=edit&mode=edit&return=plan&name=" + planId + subpage;
    return "/home?page=edit&item=" + planId;
}

function playSound(soundfile) {
    document.getElementById("dummy").innerHTML =
        "<embed src=\"" + soundfile + "\" hidden=\"true\" autostart=\"true\" loop=\"false\" />";
}

