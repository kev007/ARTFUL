var geojson;
var map;
var info;
var legend;

function initLeafletMap() {
    map = L.map('mapid').setView([51.505, -0.09], 3);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
        maxZoom: 7,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
        '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.light'
    }).addTo(map);

    $(document).ready(function () {
        $("#slider-range").slider({});
    });

    var popup = L.popup();
    function onMapClick(e) {
        console.log("You clicked on: " + e.latlng.toString());
        // popup
        //     .setLatLng(e.latlng)
        //     .setContent("You clicked the map at " + e.latlng.toString())
        //     .openOn(map);
    }

    legend = L.control({position: 'bottomright'});

    legend.onAdd = function (map) {

        var div = L.DomUtil.create('div', 'info legend'),
            grades = [
                numberWithCommas(legend1),
                numberWithCommas(legend2),
                numberWithCommas(legend3),
                numberWithCommas(legend4),
                numberWithCommas(legend5),
                numberWithCommas(legend6),
                numberWithCommas(legend7),
                numberWithCommas(legend8),
                numberWithCommas(legend9),
                numberWithCommas(legend10)
            ];
        div.innerHTML += '<h3>Legend</h3>';

        // loop through our density intervals and generate a label with a colored square for each interval
        for (var i = 0; i < grades.length; i++) {
            div.innerHTML +=
                '<i style="background:' + getColor(parseInt(grades[i].replace(/,/g, "")) + 1) + '"></i> ' +
                grades[i] + (grades[i + 1] ? ' &ndash; ' + grades[i + 1] + '<br>' : '+');
        }

        return div;
    };

    legend.addTo(map);

    info = L.control();

    info.onAdd = function (map) {
        this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
        this.update();
        return this._div;
    };

    // method that we will use to update the control based on feature properties passed
    info.update = function (props) {
        this._div.innerHTML = '<h2>Interactive Country Reference Frequency Choropleth Map</h2>' +
            '<h3>Number of references: ' +  (props ?
            '<b>' + props.name + '</b></h3>' + numberWithCommas(props.frequency)
                : '<i>none selected</i>');
    };

    info.addTo(map);

    map.on('click', onMapClick);
}

//TODO: overlapping legend levels?
var freqMax = Math.min();
var freqMin = Math.max();
var legend1 = 100000000;
var legend2 = 1000000000;
var legend3 = 2500000000;
var legend4 = 5000000000;
var legend5 = 10000000000;
var legend6 = 20000000000;
var legend7 = 30000000000;
var legend8 = 40000000000;
var legend9 = 50000000000;
var legend10 = 60000000000;
var legendCount = 11;

function calculateLegend() {
    //TODO: Algorithm stuff

    var range = freqMax - freqMin;
    var step = range / legendCount;
    var significantFigures = 2;
    // step = sigFigs(step, 2);
    // freqMin = sigFigs(freqMin, 3);
    // freqMax = sigFigs(freqMax, 3);

    legend1 = parseInt(sigFigs(freqMin, significantFigures+1)) || 0;
    legend2 = parseInt(sigFigs(freqMin + (1 * step), significantFigures)) || 0;
    legend3 = parseInt(sigFigs(freqMin + (2 * step), significantFigures)) || 0;
    legend4 = parseInt(sigFigs(freqMin + (3 * step), significantFigures)) || 0;
    legend5 = parseInt(sigFigs(freqMin + (4 * step), significantFigures)) || 0;
    legend6 = parseInt(sigFigs(freqMin + (5 * step), significantFigures)) || 0;
    legend7 = parseInt(sigFigs(freqMin + (6 * step), significantFigures)) || 0;
    legend8 = parseInt(sigFigs(freqMin + (7 * step), significantFigures)) || 0;
    legend9 = parseInt(sigFigs(freqMin + (8 * step), significantFigures)) || 0;
    legend10 = parseInt(sigFigs(freqMax, significantFigures)) || 0;
}

function sigFigs(n, sig) {
    var mult = Math.pow(10, sig - Math.floor(Math.log(n) / Math.LN10) - 1);
    // return Math.round(n * mult) / mult;
    return Math.floor(n * mult) / mult;
}

// http://colorbrewer2.org/#type=sequential&scheme=YlOrRd&n=9
function getColor(d) {
    return d > legend10 ? '#57000f' :
            d > legend9 ? '#800026' :
            d > legend8 ? '#bd0026' :
            d > legend7 ? '#e31a1c' :
            d > legend6 ? '#fc4e2a' :
            d > legend5 ? '#fd8d3c' :
            d > legend4 ? '#feb24c' :
            d > legend3 ? '#fed976' :
            d > legend2 ? '#ffeda0' :
            d > legend1 ? '#ffffcc' :
                          '#ffffff';
}

function numberWithCommas(x) {
    return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
}

function style(feature) {
    return {
        fillColor: getColor(feature.properties.frequency),
        weight: 2,
        opacity: 1,
        color: 'white',
        dashArray: '3',
        fillOpacity: 0.7
    };
}

function httpGetAsync(url, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    };
    xmlHttp.open("GET", url, true);
    xmlHttp.send();
}

var mergedData;
function mergeCountryFreq(countries, geoJSON) {
    freqMax = Math.max();
    freqMin = Math.min();
    mergedData = {
        "type": "FeatureCollection",
        "features": []
    };
    $.each(countries, function (index, country) {
        if (country.name.search(new RegExp(country.name, "i")) != -1) {
            var filter = geoJSON.features.filter(function (features) {
                return features.properties.name === country.name;
            })[0];
            if (filter) {
                var geometry = filter.geometry;
            } else {
                // console.log("no geometry for: " + country.name)
            }
            mergedData.features.push({
                "type": "Feature",
                "properties": {"name": country.name, "frequency": country.frequency},
                "geometry": geometry
            });
            if (country.frequency > freqMax) {
                freqMax = country.frequency;
            }
            if (country.frequency < freqMin) {
                freqMin = country.frequency;
            }
        }
    });

    calculateLegend();
    legend.addTo(map);

    return mergedData;
}

var layer;
function highlightFeature(e) {
    layer = e.target;

    layer.setStyle({
        weight: 5,
        color: '#666',
        dashArray: '',
        fillOpacity: 0.7
    });

    if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
        layer.bringToFront();
    }

    info.update(layer.feature.properties);
}

function resetHighlight(e) {
    geojson.resetStyle(e.target);

    info.update();
}


function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        // click: getTopTen
        click: getLanguageReferences
    });
}

// var countryReferences;
function getCountryReferences(filter) {
    // httpGetAsync('/countryRef?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
    // httpGetAsync('resources/data/country-references.js', function (response) {
    // countryReferences = JSON.parse(response);
    // });

    freqMax = Math.max();
    freqMin = Math.min();

    var beginYear = $('#slider-range').slider("values",0);
    var endYear = $('#slider-range').slider("values",1);
    var years = endYear - beginYear;

    var langIndex = countryReferences.languages.indexOf(filter);

    console.log("get: " + filter + " " + langIndex);

    for (var i = 0; i < mergedData.features.length; i++) {
    // for (i = 0; i < 1; i++) {
        var country = mergedData.features[i].properties.name;
        // console.log(country);
        var newFreq = 0;

        for (var j = 0; j < years; j++) {
            if (countryReferences.hasOwnProperty(beginYear + j) && countryReferences[beginYear + j].hasOwnProperty(country)) {
                newFreq += countryReferences[beginYear + j][country][langIndex];
            }
        }
        mergedData.features[i].properties.frequency = newFreq;
        console.log(country + ": " + newFreq);

        if (newFreq > freqMax) {
            freqMax = newFreq;
        }
        if (newFreq < freqMin) {
            freqMin = newFreq;
        }

    }

    calculateLegend();
    legend.addTo(map);

    map.removeLayer(geojson);
    geojson = L.geoJson(mergedData, {
        style: style,
        onEachFeature: onEachFeature
    }).addTo(map);
}

// var countryReferences;
function getLanguageReferences(e) {
    var beginYear = $('#slider-range').slider("values",0);
    var endYear = $('#slider-range').slider("values",1);
    if (e.hasOwnProperty("target")) {
        var country = e.target.feature.properties.name;
    } else {
        var country = e;
    }
    var years = endYear - beginYear;

    // var country  = $('#countryFilter').val();
    var length = countryReferences["languages"].length;
    var data = new Array(length).fill(0);

    for (var j = 0; j < years; j++) {
        if (countryReferences.hasOwnProperty(beginYear + j) && countryReferences[beginYear + j].hasOwnProperty(country)) {
            for (var i = 0; i < countryReferences[beginYear + j][country].length; i++) {
                data[i] += countryReferences[beginYear + j][country][i];
            }
        }
    }

    console.log("get: " + country + " " + data);
    $("code:last").html(country + ": " + data.toString());
}

function getTopTen(e) {
	var beginYear = $('#slider-range').slider("values",0);
	var endYear = $('#slider-range').slider("values",1);
	var country = e.target.feature.properties.name;
	httpGetAsync('/topTen?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
        var topTen = JSON.parse(response).countries;
        var content = "<table style=\"width:100%\"> <tr> <th>Country</th> <th>Frequency</th> </tr> ";
        for(var i = 0; i < topTen.length; i++){
        	var rec = topTen[i];
        	content += "<tr> <td> " + rec.name + "</td> <td>" + rec.frequency + "</td> </tr>";
        } 
        content += "</table>";
        
        var popup = L.popup()
        	.setLatLng(e.latlng)
        	.setContent(content)
        	.openOn(map);
    });
}

function zoomToFeature(e) {
//    map.fitBounds(e.target.getBounds());
	console.log(e.target.feature.properties.name);
}

function getFreqs(beginYear, endYear, map) {
	httpGetAsync('/freqs?start=' + beginYear + '&end=' + endYear, function (response) {
        var countryFreq = JSON.parse(response);
        var mergedData = mergeCountryFreq(countryFreq['countries'], countryData);
        geojson = L.geoJson(mergedData, {
            style: style,
            onEachFeature: onEachFeature
        }).addTo(map);
    });
}

function getTopTenMentioning(country, beginYear, endYear) {
	httpGetAsync('/topTen?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
        var countryFreq = JSON.parse(response);
        console.log(countryFreq);
    });
	
}

$(function () {
    var currYear = new Date().getFullYear();
    var handle = $( "#custom-handle" );
    var handle2 = $( "#custom-handle2" );
    $("#slider-range").slider({
        range: true,
        min: 1995,
        max: currYear,
        values: [1995, currYear],
        slide: function (event, ui) {
            handle.text( ui.values[0] );
            handle2.text( ui.values[1] );
            $("#year").val(ui.values[0] + " - " + ui.values[1]);
        },
        create: function (event, ui) {
            handle.text( $( this ).slider( "value" ) );
            handle2.text( currYear );
            getFreqs('1995', currYear, map);
        },
        change: function (event, ui) {
            map.removeLayer(geojson);
            getFreqs(ui.values[0], ui.values[1], map);
        }
    });
    $("#year").val($("#slider-range").slider("values", 0) +
        " - " + $("#slider-range").slider("values", 1));
});
