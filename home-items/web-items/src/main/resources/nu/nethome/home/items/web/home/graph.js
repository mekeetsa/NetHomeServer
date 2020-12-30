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

function ajaxFetchComplete(data) {
    if ( gCurrent >= 0 && gCurrent < gJsonUrls.length ) {
        if ( data !== null && data.length != 0 ) {
            gSeries[gSeriesLen] = data;
            if( $.type(jsonlegend) !== 'string' ) {
                gSeriesLegends[gSeriesLen] = jsonlegend[gCurrent];
            }
            gSeriesLen++;
        }
    }
    if( gCurrent >= gJsonUrls.length - 1 ) {
        // All fetched
        if (gSeriesLen > 0 ) {
            $("#chart1").html("");
            gPlotter();
        } else {
            $("#chart1").html("No data available.");
        }
    }
    else {
        if ( gSeriesLen > 0 ) { 
            gPlotter(); 
        }
        $.ajax({
            url: gJsonUrls[++gCurrent],
            dataType: "json",
            success: ajaxFetchComplete,
            context: gCurrent,
            error: function (jqXHR, textStatus) {
               $("#chart1").html("No data available. Ajax error: " + textStatus);
            }
        });
    }
}

function gPlotter() {
    $.jqplot.config.enablePlugins = true;
    var plot2 = $.jqplot('chart1', gSeries, {
        title: graph_title,
        seriesDefaults: {
            markerOptions: { show: false }
        },
        axes: {
            xaxis: {
                show: true,
                renderer: $.jqplot.DateAxisRenderer,
                tickOptions: {
                    formatString: tick_format
                }
            }
        },
        legend: {
            show: $(gSeriesLegends).length > 0,
            labels: $(gSeriesLegends),
            placement: 'insideGrid',
            location: 'w'
        },
        cursor: {
            show: true,
            tooltipLocation: 'se',
            zoom: true
        }
    });
}
// Adaptation for multiple data series (and their legends)
// Note that ajax sync fetching depricated so we use async mode.

$(document).ready(function () {

    gJsonUrls = $.type(jsonurl) === 'string' ? [jsonurl] : jsonurl;

    gSeries = [];        // holds data series
    gSeriesLegends = []; // holds data series legends
    gSeriesLen = 0;      // nr of fetched series (equal gSeries.length)
    gCurrent = -1;       // currently fetched

    ajaxFetchComplete(null); // Start async fetching
});

