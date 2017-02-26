var geojson;
var selectedGeojson;
var map;
var info;
var legend;
var selectedCountry = "";
var ingoingReferences = false;
var mergedData;
var countryFreqsOut = {};
var countryFreqsIn = {};
var maxCorporaSize = 0;
var countryFreqSum = 0;
var countryFreq;
function initLeafletMap() {
    map = L.map('mapid').setView([51.505, -0.09], 3);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoicG5hbmNrZSIsImEiOiJjaXplMXo1MWwwcmxiMnFvZXU4aDR3b2JmIn0.uP8i-2p6xfus2g7M9Y_k3Q', {
        maxZoom: 7,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
        '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.light'
    }).addTo(map);

    $(document).ready(function () {
        $("#slider-range").slider({});
    });

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
        div.innerHTML +=
            '<br/><b style="background:#478726"></b>Selected Country<br>';

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
        if (props) {
            document.getElementById('selectedCountry').textContent = selectedCountry;
            document.getElementById('hoverCountry').textContent = props.name;
            var currCountryFreq;
            var selectedReferences;
            var totalCountryReferencesSelected = 0;
            if (selectedCountry) {
                if (ingoingReferences) {
                    selectedReferences = countryFreqsIn;
                    currCountryFreq = countryFreqsIn[props.name.toLowerCase()];
                    for (var key in selectedReferences) {
                        totalCountryReferencesSelected += selectedReferences[key];
                    }
                } else {
                    selectedReferences = countryFreqsOut;
                    currCountryFreq = countryFreqsOut[props.name];
                }
                for (var key in selectedReferences) {
                    totalCountryReferencesSelected += selectedReferences[key];
                }
            } else {
                currCountryFreq = props.frequency;
                totalCountryReferencesSelected = countryFreqSum;
            }

            document.getElementById('hoverFreq').innerHTML = currCountryFreq ? numberWithCommas(currCountryFreq)
                + " (" + ((currCountryFreq / totalCountryReferencesSelected) * 100)
                    .toFixed(2) + "&#37;) "
                : numberWithCommas(props.frequency);

            document.getElementById('hoverCountry').style.display='table-cell';
            document.getElementById('hoverFreq').style.display='table-cell';
        } else {
            document.getElementById('selectedCountry').textContent = selectedCountry;
            document.getElementById('hoverCountry').textContent = "";
            document.getElementById('hoverFreq').textContent = "";

            document.getElementById('hoverCountry').style.display='none';
            document.getElementById('hoverFreq').style.display='none';
        }
        if (selectedCountry != "") {
            document.getElementById('selectedCountry').style.display='table-cell';
            document.getElementById('arrow').style.display='inline-block';
        } else {
            document.getElementById('selectedCountry').style.display='none';
            document.getElementById('arrow').style.display='none';
        }

        // this._div.innerHTML = '' +
        //     '<h2>Interactive Country Reference Frequency Choropleth Map</h2>' +
        //     '<h3>Number of references: ' +  (props ? '<b>' + props.name + '</b></h3>' + numberWithCommas(props.frequency) : '<i>none selected</i>');
    };
    info.addTo(map);
}

var freqMax = Number.MIN_VALUE;
var freqMin = Number.MAX_VALUE;
var freqMaxIngoing = Number.MIN_VALUE;
var freqMinIngoing = Number.MAX_VALUE;
var freqMaxOutgoing = Number.MIN_VALUE;
var freqMinOutgoing = Number.MAX_VALUE;
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
    var curr_min;
    var curr_max;
    if (selectedCountry) {
        if (ingoingReferences) {
            curr_min = freqMinIngoing;
            curr_max = freqMaxIngoing;
        } else {
            curr_min = freqMinOutgoing;
            curr_max = freqMaxOutgoing;
        }
    } else {
        curr_min = freqMin;
        curr_max = freqMax;
    }
    var range = curr_max - curr_min;
    var step = range / legendCount;
    var significantFigures = 2;
    // step = sigFigs(step, 2);
    // freqMin = sigFigs(freqMin, 3);
    // freqMax = sigFigs(freqMax, 3);

    legend1 = parseInt(sigFigs(curr_min, significantFigures + 1)) || 0;
    legend2 = parseInt(sigFigs(curr_min + (step), significantFigures)) || 0;
    legend3 = parseInt(sigFigs(curr_min + (2 * step), significantFigures)) || 0;
    legend4 = parseInt(sigFigs(curr_min + (3 * step), significantFigures)) || 0;
    legend5 = parseInt(sigFigs(curr_min + (4 * step), significantFigures)) || 0;
    legend6 = parseInt(sigFigs(curr_min + (5 * step), significantFigures)) || 0;
    legend7 = parseInt(sigFigs(curr_min + (6 * step), significantFigures)) || 0;
    legend8 = parseInt(sigFigs(curr_min + (7 * step), significantFigures)) || 0;
    legend9 = parseInt(sigFigs(curr_min + (8 * step), significantFigures)) || 0;
    legend10 = parseInt(sigFigs(curr_max, significantFigures)) || 0;
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
    if (x) {
        return x.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ",");
    }
    else {
        return '0'
    }
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

function styleSelectedCountry(feature) {
    return {
        fillColor: '#478726',
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

function mergeCountryFreq(countries, maxCorporaSize, geoJSON, doNormalize) {
    freqMax = Number.MIN_VALUE;
    freqMin = Number.MAX_VALUE;
    function transform(element, index) {
        element.properties.frequency = 0;
    }
    geoJSON['features'].forEach(transform);
    mergedData = geoJSON;
    countryFreqSum = 0;
    $.each(countries, function (index, country) {
        if (country.name.search(new RegExp(country.name, "i")) != -1) {
            var normalized_freq = 0;
            for (var i = 0; i < mergedData.features.length; i++) {
                if (mergedData.features[i].properties.name.toLowerCase() === country.name.toLowerCase()) {
                    var frequency_multiplier;
                    frequency_multiplier = maxCorporaSize / country.avgCorporaSize;

                    if (doNormalize) {
                        normalized_freq = Math.ceil(country.frequency * (frequency_multiplier));
                    } else {
                        normalized_freq = country.frequency;
                    }
                    mergedData.features[i].properties.frequency = normalized_freq;
                    countryFreqSum += normalized_freq;
                    break;
                }
            }
            //Ignore the selected country for legend calculation
            if (normalized_freq > freqMax && country.name.toLowerCase() !== selectedCountry) {
                if(selectedCountry && country.name.toLowerCase() !== selectedCountry.toLowerCase()){
                    freqMax = normalized_freq;
                } else if(!selectedCountry){
                    freqMax = normalized_freq;
                }
            }
            if (normalized_freq < freqMin && country.name.toLowerCase() !== selectedCountry) {
                if(selectedCountry && country.name.toLowerCase() !== selectedCountry.toLowerCase()){
                    freqMin = normalized_freq;
                } else if(!selectedCountry){
                    freqMin = normalized_freq;
                }
            }
        }
    });

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

function colorCountry(country) {
    if (selectedGeojson) {
        map.removeLayer(selectedGeojson);
    }
    selectedGeojson = L.geoJson(getGeoJson(country), {
        style: styleSelectedCountry,
        onEachFeature: onEachFeature
    }).addTo(map);
}
function resetHighlight(e) {
    geojson.resetStyle(e.target);
    if (selectedCountry) {
        colorCountry(selectedCountry);
    }

    info.update();
}


function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: proceedCountryReferences
    });
}

function removeCustomLayer() {
    if (geojson) {
        map.removeLayer(geojson);
    }
    if (selectedGeojson) {
        map.removeLayer(selectedGeojson);
    }
}

function proceedCountryReferences(clickObject) {
    removeCustomLayer();
    if (selectedCountry && selectedCountry.toLowerCase() === clickObject.target.feature.properties.name.toLowerCase()) {
        selectedCountry = '';
        var beginYear = $('#slider-range').slider("values", 0);
        var endYear = $('#slider-range').slider("values", 1);
        getFreqs(beginYear, endYear, function () {
            addGeoJsonMap(mergedData, map);
        });
    } else {
        selectedCountry = clickObject.target.feature.properties.name;
    }
    fillCountryReferences(selectedCountry);
}

function fillCountryReferences(name) {
    if (name == '') {
        console.log('No country selected')
    } else {
        console.log(name);
        if (ingoingReferences) {
            getLanguageReferences(name);
            console.log('1');
        } else {
            getCountryReferences(name);
            console.log('2');
        }
    }
    calculateLegend();
    legend.addTo(map);

    removeCustomLayer();
    addGeoJsonMap(mergedData, map, selectedCountry);
    colorCountry(selectedCountry);
    info.update();
}

function calcWeightedFrequencies () {
    //TODO: calculation

    mergedData.features[i].properties.corporaSize = newSize;
    if (newSize > freqMax && country.toLowerCase() !== selectedCountry.toLowerCase()) {
        sizeMax = newSize;
    }
    if (newSize < freqMin && country.toLowerCase() !== selectedCountry.toLowerCase()) {
        sizeMin = newSize;
    }
}

function toggleReferences() {
    ingoingReferences = !ingoingReferences;
    var arrow = document.getElementById('arrow');
    var arrowText = document.getElementById('arrowText');

    if(!ingoingReferences){
        arrow.className = 'arrow';
        arrowText.className = 'arrowText';
        arrowText.textContent = "Outgoing References";
    } else {
        arrow.className = 'arrow left';
        arrowText.className = 'arrowText left';
        arrowText.textContent = "Ingoing References";
    }

    fillCountryReferences(selectedCountry);
}

function getCorpus(country) {
    return country_language_mapping[country.toLowerCase()]
}

function getCountry(corpus) {
    return language_country_mapping[corpus.toLowerCase()]
}

function getGeoJson(country) {
    var i;
    for (i = 0; i < countryData["features"].length; i++) {
        if (countryData["features"][i]["properties"]["name"].toLowerCase() === country.toLowerCase()) {
            return {"type": "FeatureCollection", "features": [countryData["features"][i]]};
        }
    }
}

function getCountryReferences(selectedCountry) {
    countryFreqsOut = {};
    freqMaxOutgoing = Number.MIN_VALUE;
    freqMinOutgoing = Number.MAX_VALUE;

    var beginYear = $('#slider-range').slider("values",0);
    var endYear = $('#slider-range').slider("values",1);
    var years = endYear - beginYear;

    var corpus = getCorpus(selectedCountry);
    var langIndex = countryReferences.languages.indexOf(corpus);

    for (var i = 0; i < mergedData.features.length; i++) {
        var countryReferencesSum = 0;
        var country = mergedData.features[i].properties.name;
        var newFreq = 0;

        for (var j = 0; j < years; j++) {
            if (countryReferences.hasOwnProperty(beginYear + j) && countryReferences[beginYear + j].hasOwnProperty(country)) {
                var currCountryYearReferences = countryReferences[beginYear + j][country];
                newFreq += currCountryYearReferences[langIndex];
                countryReferencesSum += currCountryYearReferences.reduce(function (a, b) {
                    return a + b;
                }, 0);
            }
        }
        mergedData.features[i].properties.frequency = newFreq;
        if (newFreq > freqMaxOutgoing && country.toLowerCase() !== selectedCountry.toLowerCase()) {
            freqMaxOutgoing = newFreq;
        }
        if (newFreq < freqMinOutgoing && country.toLowerCase() !== selectedCountry.toLowerCase()) {
            freqMinOutgoing = newFreq;
        }
        countryFreqsOut[country] = newFreq;
    }
}

function getLanguageReferences(selectedCountry) {
    countryFreqsIn = {};
    var beginYear = $('#slider-range').slider("values",0);
    var endYear = $('#slider-range').slider("values",1);
    var years = endYear - beginYear;

    var length = countryReferences["languages"].length;
    var data = new Array(length).fill(0);

    var references = [];

    for (var j = 0; j < years; j++) {
        if (countryReferences.hasOwnProperty(beginYear + j) && countryReferences[beginYear + j].hasOwnProperty(selectedCountry)) {
            for (var i = 0; i < countryReferences[beginYear + j][selectedCountry].length; i++) {
                data[i] += countryReferences[beginYear + j][selectedCountry][i];
            }
        }
    }
    freqMaxIngoing = Number.MIN_VALUE;
    freqMinIngoing = Number.MAX_VALUE;
    function transform(curr_frequency, index) {
        var country_for_corpus = getCountry(countryReferences['languages'][index]);
        if (country_for_corpus) {
            var avgCorpusSizesSum = 0;
            var corpus = countryReferences['languages'][index];
            for (var j = beginYear; j < beginYear + years; j++) {
                if (!isNaN(avgCorpusSizes['corpora'][corpus]['average corpus size'][j])) {
                    avgCorpusSizesSum += avgCorpusSizes['corpora'][corpus]['average corpus size'][j]
                }
            }
            if (curr_frequency > freqMaxIngoing && country_for_corpus.toLowerCase() !== selectedCountry.toLowerCase()) {
                freqMaxIngoing = curr_frequency;
            }
            if (curr_frequency < freqMinIngoing && country_for_corpus.toLowerCase() !== selectedCountry.toLowerCase()) {
                freqMinIngoing = curr_frequency;
            }
            references.push({
                'name': country_for_corpus,
                'frequency': curr_frequency,
                'avgCorporaSize': Math.ceil(avgCorpusSizesSum / years)
            });
            countryFreqsIn[country_for_corpus] = curr_frequency;
        }
    }
    data.forEach(transform);
    mergedData = mergeCountryFreq(references, maxCorporaSize, countryData, false);
    return references;
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
	// console.log(e.target.feature.properties.name);
}

function addGeoJsonMap(data, map, country) {
    if (!country) {
        country  = "";
    }

    removeCustomLayer();
    geojson = L.geoJson(data, {
        style: style,
        onEachFeature: onEachFeature,
        filter: function (feature, layer) {
            return feature.properties.name.toLowerCase() !== country.toLowerCase();
        }
    }).addTo(map);
}

function getFreqs(beginYear, endYear, extendedCallback) {
    httpGetAsync('freqs?start=' + beginYear + '&end=' + endYear, function (response) {
        countryFreq = JSON.parse(response);
        maxCorporaSize = countryFreq['max corpora size'];
        mergedData = mergeCountryFreq(countryFreq['countries'], maxCorporaSize, countryData, true);
        calculateLegend();
        legend.addTo(map);
        extendedCallback();
    });
}

function getTopTenMentioning(country, beginYear, endYear) {
	httpGetAsync('topTen?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
        var countryFreq = JSON.parse(response);
        //console.log(countryFreq);
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
            getFreqs('1995', currYear, function () {
                addGeoJsonMap(mergedData, map);
            });
        },
        change: function (event, ui) {
            map.removeLayer(geojson);
            if (selectedGeojson) {
                map.removeLayer(selectedGeojson);
            }
            getFreqs(ui.values[0], ui.values[1], function () {
                addGeoJsonMap(mergedData, map);
            });
            // fillCountryReferences(selectedCountry);
        }
    });
    $("#year").val($("#slider-range").slider("values", 0) +
        " - " + $("#slider-range").slider("values", 1));
});
