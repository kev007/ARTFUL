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
    <script src="<c:url value="/resources/js/jquery-ui-1.12.1/jquery-ui.js" />"></script>
    <script src="<c:url value="/resources/js/app.js" />"></script>
    <script src="<c:url value="/resources/js/leaflet.js" />"></script>
</head>

<body>
    <map id="mapid"></map>

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
    </controls>

    <script>initLeafletMap();</script>
</body>

</html>
