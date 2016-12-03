function initLeafletMap() {
    var map = L.map('mapid').setView([51.505, -0.09], 3);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoibWFwYm94IiwiYSI6ImNpandmbXliNDBjZWd2M2x6bDk3c2ZtOTkifQ._QA7i5Mpkd_m30IGElHziw', {
        maxZoom: 18,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
        '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.light'
    }).addTo(map);

    var popup = L.popup();

    httpGetAsync('/freqs', function (response) {
        var countryFreq = JSON.parse(response);
        var mergedData = mergeCountryFreq(countryFreq['countries'], countryData);
        L.geoJson(mergedData, {style: style}).addTo(map);
    });

    function onMapClick(e) {
        popup
            .setLatLng(e.latlng)
            .setContent("You clicked the map at " + e.latlng.toString())
            .openOn(map);
    }

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

function httpGetAsync(theUrl, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    };
    xmlHttp.open("GET", theUrl, true);
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
