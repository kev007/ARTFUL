<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<title>Interactive Country Reference Frequency Choropleth Map</title>
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="<c:url value="/resources/css/leaflet.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/jquery-ui.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/main.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/js/jquery-3.1.1.min.js" />"></script>
    <script src="<c:url value="/resources/data/country-data.js" />"></script>
    <script src="<c:url value="/resources/data/country-references.js" />"></script>
    <script src="<c:url value="/resources/data/language-country-mapping.js" />"></script>
    <script src="<c:url value="/resources/data/country-language-mapping.js" />"></script>
    <script src="<c:url value="/resources/data/average-corpus-sizes.js" />"></script>
    <script src="<c:url value="/resources/js/jquery-ui-1.12.1/jquery-ui.js" />"></script>
    <script src="<c:url value="/resources/js/app.js" />"></script>
    <script src="<c:url value="/resources/js/leaflet.js" />"></script>
    <script src="<c:url value="/resources/js/jquery-csv.min.js" />"></script>
</head>

<body>
    <map id="mapid"></map>

    <%--<debug>--%>
        <%--[TEST] Language filter: <input type="text" id="langFilter" value="eng"/>--%>
        <%--<input type="submit" value="Submit" onClick="getCountryReferences($('#langFilter').val())">--%>
        <%--<input type="submit" value="[DEBUG]" onClick="console.log(countryReferences.languages), $('code:last').html('languages: ' + countryReferences.languages.toString())">--%>
        <%--<br>--%>
        <%--[TEST] Country filter: <input type="text" id="countryFilter" value="Germany"/>--%>
        <%--<input type="submit" value="Submit" onClick="getLanguageReferences($('#countryFilter').val())">--%>
        <%--<br>--%>
        <%--<div>--%>
            <%--<code>--%>
                <%--[DEBUG]--%>
            <%--</code>--%>
        <%--</div>--%>

        <%--<br>--%>
        <%--<label>--%>
            <%--<input type="radio" name="references-radio" value="ingoing" checked>--%>
        <%--</label>Ingoing References--%>
        <%--<label>--%>
            <%--<input type="radio" name="references-radio" value="outgoing">--%>
        <%--</label>Outgoing References--%>

        <%--<br>--%>
        <%--<div id="arrow" class="arrow" onClick="toggleReferences()">--%>
            <%--<span id="arrowText" class="arrowText">Outgoing References</span>--%>
        <%--</div>--%>
    <%--</debug>--%>
    <description>
        <title>ARTFUL</title>
        <h1><span style="color: #4D81BF">A</span>n inte<span style="color: #4D81BF">r</span>active coun<span style="color: #4D81BF">t</span>ry re<span style="color: #4D81BF">f</span>erence freq<span style="color: #4D81BF">u</span>ency chorop<span style="color: #4D81BF">l</span>eth mapper</h1>

    </description>

    <controls>
        <%--<b>${message}</b>--%>
        <%--<p>--%>
            <%--<label for="year">Selected years</label>--%>
            <%--<input type="text" id="year" readonly style="border:0; color:#f6931f; font-weight:bold;">--%>
        <%--</p>--%>
        <div id="slider-range">
            <div id="custom-handle" class="ui-slider-handle"></div>
            <div id="custom-handle2" class="ui-slider-handle"></div>
        </div>

        <br>
        <div id="arrowContainer" style="display:table; height: 45px">
            <span id="selectedCountry" class="countryText" style="display: none">COUNTRY 1</span>
            <div id="arrow" class="arrow" onClick="handleToggleReferences()" style="display: none">
               <span id="arrowText" class="arrowText">Outgoing References</span>
            </div>
            <span id="hoverCountry" class="countryText">Select a Country</span>
        </div>
        <span id="hoverFreq" class="countryText"></span>
    </controls>

    <script>
        initLeafletMap();
    </script>

</body>

</html>
