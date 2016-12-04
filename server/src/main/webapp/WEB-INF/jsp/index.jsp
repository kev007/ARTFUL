<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="<c:url value="/resources/leaflet.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/jquery-ui-1.12.1/jquery-ui.css" />" rel="stylesheet">
    <link href="<c:url value="/resources/css/app.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/jquery-3.1.1.min.js" />"></script>
    <script src="<c:url value="/resources/data/country-data.js" />"></script>
    <script src="<c:url value="/resources/jquery-ui-1.12.1/jquery-ui.js" />"></script>
    <script src="<c:url value="/resources/app.js" />"></script>
    <script src="<c:url value="/resources/leaflet.js" />"></script>
</head>

<body>
    <b>${message}</b>
    <p>
        <label for="year">Selected years</label>
        <input type="text" id="year" readonly style="border:0; color:#f6931f; font-weight:bold;">
    </p>
    <div id="slider-range" style="width: 300px"></div>
    <br/>
    <div id="mapid" style="width: 800px; height: 500px;"></div>
    <script>initLeafletMap();</script>
</body>

</html>
