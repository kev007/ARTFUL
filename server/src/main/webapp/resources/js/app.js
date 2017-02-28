/**
 * IMPORTANT VARIABLES
 */
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
var startYear = 1995;
var endYear = new Date().getFullYear();
var numOfYears;

var instantSlider = true; //change to false to reduce the number of request to the server
var verboseLogging = true;
var useLogarithmicLegend = false;

/**
 * *********************************************************************************************************************
 * LEAFLET
 * *********************************************************************************************************************
 */
function initLeafletMap() {
    map = L.map('mapid').setView([51.505, -0.09], 3);

    L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1IjoicG5hbmNrZSIsImEiOiJjaXplMXo1MWwwcmxiMnFvZXU4aDR3b2JmIn0.uP8i-2p6xfus2g7M9Y_k3Q', {
        maxZoom: 7,
        attribution: 'Map data &copy; <a href="http://openstreetmap.org">OpenStreetMap</a> contributors, ' +
        '<a href="http://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, ' +
        'Imagery © <a href="http://mapbox.com">Mapbox</a>',
        id: 'mapbox.light'
    }).addTo(map);

    legend = L.control({position: 'bottomright'});

    legend.onAdd = function () {
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
        if (useLogarithmicLegend) {
            div.innerHTML += '<h3>Legend <span class="ui-icon ui-icon-arrowreturnthick-1-n"></span>Logarithmic </h3>';
        } else {
            div.innerHTML += '<h3>Legend <span class="ui-icon ui-icon-arrowthick-1-ne"></span>Linear </h3>';
        }


        // loop through our density intervals and generate a label with a colored square for each interval
        for (var i = 0; i < grades.length; i++) {
            div.innerHTML +=
                '<i style="background:' + getColor(parseInt(grades[i].replace(/,/g, "")) + 1) + '"></i> ' +
                grades[i] + (grades[i + 1] ? ' &ndash; ' + grades[i + 1] + '<br>' : '+');
        }
        div.innerHTML +=
            '<br/><b style="background:#478726"></b>Selected Country<br>'; //478726, 79a662

        if (useLogarithmicLegend) {
            div.innerHTML += '<br><a href="#" id="legendTitle" onClick="handleToggleLegendMode()">Switch to linear</a>';
        } else {
            div.innerHTML += '<br><a href="#" id="legendTitle" onClick="handleToggleLegendMode()">Switch to logarithmic</a>';
        }

        return div;
    };

    legend.addTo(map);

    info = L.control();

    info.onAdd = function () {
        this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
        this.update();
        return this._div;
    };

    // method that we will use to update the control based on feature properties passed
    info.update = function (props) {
        if (props) {
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
            if (selectedCountry) {
                document.getElementById('selectedCountry').textContent = selectedCountry;
                document.getElementById('hoverCountry').textContent = props.name;
                document.getElementById('hoverFreq').innerHTML = currCountryFreq ? numberWithCommas(currCountryFreq)
                    + " (" + ((currCountryFreq / totalCountryReferencesSelected) * 100)
                        .toFixed(2) + "&#37;) "
                    : numberWithCommas(props.frequency);
            } else {
                document.getElementById('selectedCountry').textContent = props.name;
                // document.getElementById('hoverCountry').textContent = props.name;
                document.getElementById('hoverFreq').innerHTML = currCountryFreq ? numberWithCommas(currCountryFreq)
                    + " (" + ((currCountryFreq / totalCountryReferencesSelected) * 100)
                        .toFixed(2) + "&#37;) "
                    : numberWithCommas(props.frequency);
            }



            document.getElementById('hoverCountry').style.display='table-cell';
            document.getElementById('hoverFreq').style.display='table-cell';
        } else {
            document.getElementById('selectedCountry').textContent = selectedCountry;
            document.getElementById('hoverCountry').textContent = "";
            document.getElementById('hoverFreq').textContent = "";

            document.getElementById('hoverCountry').style.display='none';
            document.getElementById('hoverFreq').style.display='none';
        }
    };
    info.addTo(map);
}

function onEachFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: handleCountrySelect
    });
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
    if (selectedCountry) {
        colorCountry(selectedCountry);
    }

    info.update();
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

/**
 * *********************************************************************************************************************
 * SLIDER
 * *********************************************************************************************************************
 */
$(function () {
    var handleLeft = $( "#custom-handle" );
    var handleRight = $( "#custom-handle2" );
    var slider = $("#slider-range");
    slider.slider({
        range: true,
        min: startYear,
        max: endYear,
        values: [startYear, endYear],
        create: function () {
            handleLeft.text(startYear);
            handleRight.text(endYear);

            handleYearChange(startYear, endYear);
        },
        slide: function (event, ui) {
            handleLeft.text(ui.values[0]);
            handleRight.text(ui.values[1]);
            $("#year").val(ui.values[0] + " - " + ui.values[1]);

            if(instantSlider) {
                handleYearChange(ui.values[0], ui.values[1]);
            }
        },
        change: function (event, ui) {
            if(!instantSlider) {
                handleYearChange(ui.values[0], ui.values[1]);
            }
        }
    });
    $("#year").val(slider.slider("values", 0) +
        " - " + slider.slider("values", 1));
});


/**
 * *********************************************************************************************************************
 * LOGIC
 * *********************************************************************************************************************
 */

/**
 * ***********************************************************
 * HANDLE USER INPUTS
 * ***********************************************************
 */

/**
 * When the user changes the year:
 *
 * @param start
 * @param end
 */
function handleYearChange(start, end) {
    startYear = start;
    endYear = end;
    numOfYears = end - start + 1; //+1 for inclusive year e.g. 2015 to 2015 should include 2015 i.e. 1 year

    if (selectedCountry) {
        //case: a country is selected, year changed
        doDirectionalCountryReferences();
    } else {
        //case: no country selected, year changed
        doReferenceTotals();
    }
}

/**
 * When the user flips the reference direction arrow:
 *
 */
function handleToggleReferences() {
    ingoingReferences = !ingoingReferences;

    if(selectedCountry) {
        //case: a country is selected, direction changed
        doDirectionalCountryReferences();
    } else {
        //case: a country is not selected, direction changed
        doReferenceTotals();
    }
}

/**
 * When the user flips the reference direction arrow:
 *
 */
function handleToggleLegendMode() {
    useLogarithmicLegend = !useLogarithmicLegend;

    updateGUI();
}

/**
 * When the user clicks on a country:
 * 1. If a country is not selected, select it
 * 2. If the user clicks on a selected country, deselect it
 * 3. If a country is selected and the user selects a different country, select the different country instead
 *
 * @param clickObject
 */
function handleCountrySelect(clickObject) {
    if (selectedCountry) { //case: country is selected
        if (selectedCountry.toLowerCase() === clickObject.target.feature.properties.name.toLowerCase()) { //case: selected country is reselected
            selectedCountry = ""; //deselect the country
            doReferenceTotals(); //display all references (non-directional, total references)
        } else {  //case: different country is selected
            selectedCountry = clickObject.target.feature.properties.name; //select a different country
            doDirectionalCountryReferences();
        }
    } else { //case: country is not selected
        selectedCountry = clickObject.target.feature.properties.name; //select a country
        doDirectionalCountryReferences();
    }
}


/**
 * ***********************************************************
 * HANDLE DATA LOGIC
 * ***********************************************************
 */
/**
 * Decides which direction the reference is, executes corresponding calculation
 */
function doDirectionalCountryReferences() {
    if (selectedCountry) {
        if (ingoingReferences) {
            getLanguageReferences();
            if (verboseLogging) console.log(selectedCountry + ': Ingoing References: ' + startYear + '-' + endYear);
        } else {
            getCountryReferences();
            if (verboseLogging) console.log(selectedCountry + ': Outgoing References: ' + startYear + '-' + endYear);
        }
    } else {
        if (verboseLogging) console.log("getCountryReferenceState: something broke or this function was called incorrectly")
    }
    updateGUI();
}

/**
 * Read all ingoing references. do stuff
 * @returns {Array}
 */
function getLanguageReferences() {
    countryFreqsIn = {};

    var length = countryReferences["languages"].length;
    var data = new Array(length).fill(0);

    var references = [];

    for (var y = 0; y < numOfYears; y++) {
        if (countryReferences.hasOwnProperty(startYear + y) && countryReferences[startYear + y].hasOwnProperty(selectedCountry)) {
            for (var c = 0; c < countryReferences[startYear + y][selectedCountry].length; c++) {
                data[c] += countryReferences[startYear + y][selectedCountry][c];
            }
        }
    }
    freqMaxIngoing = 0;
    freqMinIngoing = Number.MAX_VALUE;
    function transform(curr_frequency, index) {
        var country_for_corpus = getCountry(countryReferences['languages'][index]);
        if (country_for_corpus) {
            var avgCorpusSizesSum = 0;
            var corpus = countryReferences['languages'][index];
            for (var y = startYear; y < startYear + numOfYears; y++) {
                if (!isNaN(avgCorpusSizes['corpora'][corpus]['average corpus size'][y])) {
                    avgCorpusSizesSum += avgCorpusSizes['corpora'][corpus]['average corpus size'][y]
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
                'ingoing frequency': curr_frequency,
                'avgCorporaSizeIngoing': Math.ceil(avgCorpusSizesSum / numOfYears)
            });
            countryFreqsIn[country_for_corpus] = curr_frequency;
        }
    }
    data.forEach(transform);
    mergedData = mergeCountryFreq(references, maxCorporaSize, countryData, false);
    return references;
}

/**
 * Read all outgoing references. do stuff.
 */
function getCountryReferences() {
    countryFreqsOut = {};
    freqMaxOutgoing = 0;
    freqMinOutgoing = Number.MAX_VALUE;

    var corpus = getCorpus(selectedCountry);
    var langIndex = countryReferences.languages.indexOf(corpus);

    for (var i = 0; i < mergedData.features.length; i++) {
        var countryReferencesSum = 0;
        var country = mergedData.features[i].properties.name;
        var newFreq = 0;

        for (var y = 0; y < numOfYears; y++) {
            if (countryReferences.hasOwnProperty(startYear + y) && countryReferences[startYear + y].hasOwnProperty(country)) {
                var currCountryYearReferences = countryReferences[startYear + y][country];
                newFreq += currCountryYearReferences[langIndex];
                countryReferencesSum += currCountryYearReferences.reduce(function (a, b) {
                    return a + b;
                }, 0);
            }
        }
        if (isNaN(newFreq)) {
            newFreq = 0;
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

/**
 * Gets all country reference frequencies (non-directional, total references) from the REST API.
 * @param start
 * @param end
 */
function doReferenceTotals() {
    if (verboseLogging) console.log(selectedCountry + 'All Countries: ' + startYear + '-' + endYear);

    httpGetAsync('freqs?start=' + startYear + '&end=' + endYear, function (response) {
        countryFreq = JSON.parse(response);
        var maxCorporaSize = countryFreq['max corpora size'];

        mergedData = mergeCountryFreq(countryFreq['countries'], maxCorporaSize, countryData, true);

        updateGUI();
    });
}

/**
 * Asynchronous HTTP GET to the API provided by the server at 'url'
 * @param url
 * @param callback
 */
function httpGetAsync(url, callback) {
    var xmlHttp = new XMLHttpRequest();
    xmlHttp.onreadystatechange = function () {
        if (xmlHttp.readyState == 4 && xmlHttp.status == 200)
            callback(xmlHttp.responseText);
    };
    xmlHttp.open("GET", url, true);
    xmlHttp.send();
}

/**
 * merges data sets
 * @param countries
 * @param maxCorporaSize
 * @param geoJSON
 * @param doNormalize
 * @returns {*}
 */
function mergeCountryFreq(countries, maxCorporaSize, geoJSON, doNormalize) {
    freqMax = 0;
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
                    var averageCorporaSize;
                    var countryFrequency;
                    if (ingoingReferences) {
                        averageCorporaSize = country.avgCorporaSizeOutgoing;
                        if(averageCorporaSize == 0) {
                            averageCorporaSize = countryFreq['max corpora size ingoing']/2;
                        }
                        countryFrequency = country['ingoing frequency'];
                    } else {
                        averageCorporaSize = country.avgCorporaSizeIngoing;
                        countryFrequency = country['outgoing frequency'];
                        if(averageCorporaSize == 0) {
                            averageCorporaSize = countryFreq['max corpora size outgoing']/2;
                        }
                    }

                    frequency_multiplier = maxCorporaSize / averageCorporaSize;
                    // console.log(averageCorporaSize + country.name + countryFrequency);

                    if (doNormalize) {
                        normalized_freq = Math.ceil(countryFrequency * (frequency_multiplier));
                    } else {
                        normalized_freq = countryFrequency;
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

/**
 * Function responsible for refreshing all GUI elements:
 * 1. Leaflet custom layer
 * 2. Legend
 * 3. Frequency readout (except hover info, see leaflet info.update())
 * 4. Reference direction arrow
 */
function updateGUI() {
    //remove custom layer
    if (geojson) {
        map.removeLayer(geojson);
    }
    if (selectedGeojson) {
        map.removeLayer(selectedGeojson);
    }

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

    if (selectedCountry) {
        document.getElementById('selectedCountry').style.display='table-cell';
        document.getElementById('arrow').style.display='inline-block';
    } else {
        // document.getElementById('selectedCountry').style.display='none';
        document.getElementById('selectedCountry').style.display='table-cell';
        // document.getElementById('arrow').style.display='none';
        document.getElementById('arrow').style.display='inline-block';
    }

    calculateLegend();
    legend.addTo(map);

    if (selectedCountry) {
        colorCountry(selectedCountry);
        addGeoJsonMap(mergedData, map, selectedCountry);
    } else {
        addGeoJsonMap(mergedData, map);
    }
}

function addGeoJsonMap(data, map, filterCountry) {
    if (!filterCountry) {
        filterCountry  = "";
    }

    geojson = L.geoJson(data, {
        style: style,
        onEachFeature: onEachFeature,
        filter: function (feature, layer) {
            return feature.properties.name.toLowerCase() !== filterCountry.toLowerCase();
        }
    }).addTo(map);
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



function getCorpus(country) {
    return country_language_mapping[country.toLowerCase()]
}

function getCountry(corpus) {
    return language_country_mapping[corpus.toLowerCase()]
}

function getGeoJson(country) {
    for (var i = 0; i < countryData["features"].length; i++) {
        if (countryData["features"][i]["properties"]["name"].toLowerCase() === country.toLowerCase()) {
            return {"type": "FeatureCollection", "features": [countryData["features"][i]]};
        }
    }
}

/**
 * *********************************************************************************************************************
 * LEGEND
 * *********************************************************************************************************************
 */
var freqMax = 0;
var freqMin = Number.MAX_VALUE;
var freqMaxIngoing = 0;
var freqMinIngoing = Number.MAX_VALUE;
var freqMaxOutgoing = 0;
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
var legendCount = 10;

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

    if (useLogarithmicLegend) {
        legend1 = logScale(curr_min, curr_max, legendCount, 1) || 0;
        legend2 = logScale(curr_min, curr_max, legendCount, 2) || 0;
        legend3 = logScale(curr_min, curr_max, legendCount, 3) || 0;
        legend4 = logScale(curr_min, curr_max, legendCount, 4) || 0;
        legend5 = logScale(curr_min, curr_max, legendCount, 5) || 0;
        legend6 = logScale(curr_min, curr_max, legendCount, 6) || 0;
        legend7 = logScale(curr_min, curr_max, legendCount, 7) || 0;
        legend8 = logScale(curr_min, curr_max, legendCount, 8) || 0;
        legend9 = logScale(curr_min, curr_max, legendCount, 9) || 0;
        legend10 = logScale(curr_min, curr_max, legendCount, 10) || 0;
    } else {
        legend1 = sigFigs(curr_min, significantFigures + 1) || 0;
        legend2 = sigFigs(curr_min + (step), significantFigures) || 0;
        legend3 = sigFigs(curr_min + (2 * step), significantFigures) || 0;
        legend4 = sigFigs(curr_min + (3 * step), significantFigures) || 0;
        legend5 = sigFigs(curr_min + (4 * step), significantFigures) || 0;
        legend6 = sigFigs(curr_min + (5 * step), significantFigures) || 0;
        legend7 = sigFigs(curr_min + (6 * step), significantFigures) || 0;
        legend8 = sigFigs(curr_min + (7 * step), significantFigures) || 0;
        legend9 = sigFigs(curr_min + (8 * step), significantFigures) || 0;
        legend10 = sigFigs(curr_max, significantFigures) || 0;
    }
}

function logScale(min, max, legendCount, n) {
    if (min == 0) {
        min = 1;
    }
    var minv = Math.log(min);
    var maxv = Math.log(max);

    var scale = (maxv - minv) / (legendCount);
    var result = Math.exp(min + scale * (n - min));

    return sigFigs(result, 2);
}

/**
 * Apply n significant figure rounding to value
 *
 * @param value
 * @param n
 * @returns {number}
 */
function sigFigs(value, n) {
    var mult = Math.pow(10, n - Math.floor(Math.log(value) / Math.LN10) - 1);
    var result = Math.floor(value * mult) / mult;
    result = parseInt(result);

    //x,999 parseInt funny business workaround
    if (result % 10 == 9) {
        result++;
    }

    return result;
}


/**
 * Color palette for the legend.
 * @param d
 * @returns {string}
 */
function getColor(d) {
    // http://colorbrewer2.org/#type=sequential&scheme=YlOrRd&n=9

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







/**
 * *********************************************************************************************************************
 * DEPRECATED FUNCTIONS
 * This is the function graveyard. If they're useful, resurrect them!
 * *********************************************************************************************************************
 */

// function getTopTenMentioning(country, beginYear, endYear) {
//     httpGetAsync('topTen?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
//         var countryFreq = JSON.parse(response);
//         //console.log(countryFreq);
//     });
// }

// function getTopTen(e) {
//     var beginYear = $('#slider-range').slider("values",0);
//     var endYear = $('#slider-range').slider("values",1);
//     var country = e.target.feature.properties.name;
//     httpGetAsync('/topTen?country=' + country + '&start=' + beginYear + '&end=' + endYear, function (response) {
//         var topTen = JSON.parse(response).countries;
//         var content = "<table style=\"width:100%\"> <tr> <th>Country</th> <th>Frequency</th> </tr> ";
//         for(var i = 0; i < topTen.length; i++){
//             var rec = topTen[i];
//             content += "<tr> <td> " + rec.name + "</td> <td>" + rec.frequency + "</td> </tr>";
//         }
//         content += "</table>";
//
//         var popup = L.popup()
//             .setLatLng(e.latlng)
//             .setContent(content)
//             .openOn(map);
//     });
// }

// function zoomToFeature(e) {
//     map.fitBounds(e.target.getBounds());
//     console.log(e.target.feature.properties.name);
// }

// $(document).ready(function () {
//     $("#slider-range").slider({});
// });

// function getFreqs(beginYear, endYear, extendedCallback) {
//     httpGetAsync('freqs?start=' + beginYear + '&end=' + endYear, function (response) {
//         countryFreq = JSON.parse(response);
//         maxCorporaSize = countryFreq['max corpora size'];
//         mergedData = mergeCountryFreq(countryFreq['countries'], maxCorporaSize, countryData, true);
//
//         extendedCallback();
//     });
// }

// function removeCustomLayer() {
//     if (geojson) {
//         map.removeLayer(geojson);
//     }
//     if (selectedGeojson) {
//         map.removeLayer(selectedGeojson);
//     }
// }