var geojson;
var map;
var info;

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

    var legend = L.control({position: 'bottomright'});

    legend.onAdd = function (map) {

        var div = L.DomUtil.create('div', 'info legend'),
            grades = [0, 10, 20, 50, 100, 200, 500, 1000],
            // grades = [1000, 500, 200, 100, 50, 20, 10, 0],
            labels = [];

        div.innerHTML += '<h3>Legend</h3>';

        // loop through our density intervals and generate a label with a colored square for each interval
        for (var i = 0; i < grades.length; i++) {
            div.innerHTML +=
                '<i style="background:' + getColor(grades[i] + 1) + '"></i> ' +
                grades[i] + (grades[i + 1] ? '&ndash;' + grades[i + 1] + '<br>' : '+');
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
    //TODO: properly recieve the passed frequency
    info.update = function (props) {
        this._div.innerHTML = '<h2>Interactive Country Reference Frequency Choropleth Map</h2>' +
            '<h3>Number of references to {SELECTED COUNTRY}</h3>' +  (props ?
            '<b>' + props.name + '</b><br />' + props.freq
                : 'Hover over a country');
    };

    info.addTo(map);

    map.on('click', onMapClick);
}

function getColor(d) {
    return d > 1000 ? '#800026' :
        d > 500  ? '#BD0026' :
            d > 200  ? '#E31A1C' :
                d > 100  ? '#FC4E2A' :
                    d > 50   ? '#FD8D3C' :
                        d > 20   ? '#FEB24C' :
                            d > 10   ? '#FED976' :
                                '#FFEDA0';
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

function mergeCountryFreq(countries, geoJSON) {
    var mergedData = {
        "type": "FeatureCollection",
        "features": []
    };
    $.each(countries, function (index, country) {
        if (country.name.search(new RegExp(country.name, "i")) != -1) {
            var geometry = geoJSON.features.filter(function (features) {
                return features.properties.name === country.name;
            })[0].geometry;

            mergedData.features.push({
                "type": "Feature",
                "properties": {"name": country.name, "frequency": country.frequency},
                "geometry": geometry
            });
        }
    });
    return mergedData;
}

function highlightFeature(e) {
    var layer = e.target;

    layer.setStyle({
        weight: 5,
        color: '#666',
        dashArray: '',
        fillOpacity: 0.7
    });

    if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
        layer.bringToFront();
    }

    info.update(layer.feature.properties); //TODO: pass the correct properties
}

function resetHighlight(e) {
    geojson.resetStyle(e.target);

    info.update();
}


function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight//,
        // click: zoomToFeature
    });
}


function zoomToFeature(e) {
    map.fitBounds(e.target.getBounds());
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

$(function () {
    var currYear = new Date().getFullYear();
    $("#slider-range").slider({
        range: true,
        min: 1995,
        max: currYear,
        values: [1995, currYear],
        slide: function (event, ui) {
            $("#year").val(ui.values[0] + " - " + ui.values[1]);
        },
        create: function (event, ui) {
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
