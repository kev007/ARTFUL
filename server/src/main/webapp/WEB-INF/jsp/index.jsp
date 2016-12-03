<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html lang="en">
<head>
    <meta charset="utf-8"/>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <link href="<c:url value="/resources/leaflet.css" />" rel="stylesheet">
    <script src="<c:url value="/resources/app.js" />"></script>
    <script src="<c:url value="/resources/leaflet.js" />"></script>
</head>

<body>
    <b>${message}</b>
    <div id="mapid" style="width: 600px; height: 400px;"></div>
    <script>initLeafletMap();</script>
</body>

</html>
