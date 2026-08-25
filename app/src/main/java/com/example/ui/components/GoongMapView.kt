package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Quest
import com.example.model.QuestStop
import com.example.model.StopStatus
import com.example.ui.theme.ClayOrange
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.GrabGreen
import com.example.ui.theme.Ink600
import com.example.ui.theme.Ink900
import com.example.ui.theme.PaperWhite
import com.example.ui.theme.SunGold
import com.example.util.CompassOrientationTracker
import com.example.util.LocationTracker
import com.example.util.l
import kotlinx.coroutines.flow.emptyFlow

class GoongJsBridge(var onStopSelected: (String) -> Unit) {
    @JavascriptInterface
    fun selectStop(stopId: String) {
        onStopSelected(stopId)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoongMapView(
    quest: Quest?,
    selectedStop: QuestStop?,
    onSelectStop: (QuestStop) -> Unit,
    userLatitude: Double? = null,
    userLongitude: Double? = null,
    onSimulateStep: (() -> Unit)? = null,
    isJourneyStarted: Boolean = false,
    modifier: Modifier = Modifier,
    currentLanguage: String = "vi"
) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Realtime location updates
    val locationTracker = remember { LocationTracker(context) }
    val locationFlow = remember(hasLocationPermission) {
        if (hasLocationPermission) locationTracker.getLocationFlow() else emptyFlow()
    }
    val gpsLocation by locationFlow.collectAsStateWithLifecycle(initialValue = null)

    // Realtime device compass heading for User POV Cone
    val compassTracker = remember { CompassOrientationTracker(context) }
    val compassFlow = remember { compassTracker.getHeadingFlow() }
    val deviceHeading by compassFlow.collectAsStateWithLifecycle(initialValue = 0f)

    // Camera follow / POV mode switch
    var isPovFollowMode by remember { mutableStateOf(isJourneyStarted) }

    // Current active user coordinates
    val activeUserLat = userLatitude ?: gpsLocation?.latitude ?: quest?.stops?.firstOrNull()?.latitude?.minus(0.0015) ?: 10.7741
    val activeUserLng = userLongitude ?: gpsLocation?.longitude ?: quest?.stops?.firstOrNull()?.longitude?.minus(0.0012) ?: 106.7028

    val ongoingStop = quest?.stops?.firstOrNull { it.status == StopStatus.CURRENT }
        ?: quest?.stops?.firstOrNull { it.status != StopStatus.COMPLETED && it.status != StopStatus.SKIPPED }
        ?: selectedStop
        ?: quest?.stops?.firstOrNull()

    val shouldCenter = remember { arrayOf(false) }
    val shouldFitRoute = remember { arrayOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("goong_map_view")
    ) {
        val goongApiKey = remember {
            try {
                com.example.BuildConfig.GOONG_API_KEY.removeSurrounding("\"").trim()
            } catch (e: Throwable) {
                ""
            }
        }

        val goongMaptilesKey = remember {
            try {
                com.example.BuildConfig.GOONG_MAPTILES_KEY.removeSurrounding("\"").trim()
            } catch (e: Throwable) {
                ""
            }
        }

        val ndaMapsApiKey = remember {
            try {
                com.example.BuildConfig.NDAMAPS_API_KEY.removeSurrounding("\"").trim()
            } catch (e: Throwable) {
                ""
            }
        }

        val centerLat = ongoingStop?.latitude ?: quest?.stops?.firstOrNull()?.latitude ?: 10.7769
        val centerLng = ongoingStop?.longitude ?: quest?.stops?.firstOrNull()?.longitude ?: 106.7009

        val markersJson = org.json.JSONArray().apply {
            quest?.stops?.forEachIndexed { index, stop ->
                val symbol = when (stop.category.lowercase()) {
                    "culinary", "ẩm thực", "food" -> "🍜"
                    "history", "lịch sử" -> "🏛️"
                    "coffee", "cà phê" -> "☕"
                    "architecture", "kiến trúc" -> "🏮"
                    "art", "nghệ thuật" -> "🎨"
                    else -> "📍"
                }
                put(org.json.JSONObject().apply {
                    put("id", stop.id)
                    put("name", stop.name)
                    put("category", stop.category)
                    put("lat", stop.latitude)
                    put("lng", stop.longitude)
                    put("status", stop.status.name)
                    put("num", index + 1)
                    put("symbol", symbol)
                })
            }
        }.toString()

        val lineCoordinatesJson = if (isJourneyStarted && quest != null && ongoingStop != null) {
            org.json.JSONArray().apply {
                val stops = quest.stops
                val ongoingIndex = stops.indexOfFirst { it.id == ongoingStop.id }
                if (ongoingIndex <= 0) {
                    put(org.json.JSONArray().apply {
                        put(activeUserLat)
                        put(activeUserLng)
                    })
                    put(org.json.JSONArray().apply {
                        put(ongoingStop.latitude)
                        put(ongoingStop.longitude)
                    })
                } else {
                    val prevStop = stops[ongoingIndex - 1]
                    put(org.json.JSONArray().apply {
                        put(prevStop.latitude)
                        put(prevStop.longitude)
                    })
                    put(org.json.JSONArray().apply {
                        put(ongoingStop.latitude)
                        put(ongoingStop.longitude)
                    })
                }
            }.toString()
        } else {
            "[]"
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="utf-8" />
                <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
                <meta name="color-scheme" content="light" />
                <!-- Vector Map Engine (NDAMaps & Goong Maps Standard) -->
                <link rel="stylesheet" href="https://unpkg.com/maplibre-gl@3.6.2/dist/maplibre-gl.css" />
                <script src="https://unpkg.com/maplibre-gl@3.6.2/dist/maplibre-gl.js"></script>
                <style>
                    * { margin: 0; padding: 0; box-sizing: border-box; }
                    html, body, #map {
                        width: 100%;
                        height: 100%;
                        overflow: hidden;
                        background-color: #E2E8F0;
                        font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    }
                    .marker-stop-wrapper {
                        position: relative;
                        cursor: pointer;
                        width: 44px;
                        height: 44px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                    }
                    .marker-stop {
                        width: 36px;
                        height: 36px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        font-size: 18px;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.25);
                        border: 2.5px solid #FFFFFF;
                        transition: transform 0.2s cubic-bezier(0.34, 1.56, 0.64, 1);
                    }
                    .marker-stop:active {
                        transform: scale(1.18);
                    }
                    .marker-stop.current {
                        background: #00B14F;
                        animation: pulse-ring 1.8s infinite;
                    }
                    .marker-stop.completed {
                        background: #2D6A4F;
                    }
                    .marker-stop.locked {
                        background: #E53935;
                    }
                    .marker-number-badge {
                        position: absolute;
                        top: -2px;
                        right: -2px;
                        background: #FFB800;
                        color: #1A1A1A;
                        font-size: 10px;
                        font-weight: 900;
                        width: 17px;
                        height: 17px;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        border: 1.5px solid #FFFFFF;
                        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
                    }
                    .marker-number-badge.completed-badge {
                        background: #2D6A4F;
                        color: #FFFFFF;
                    }
                    .marker-number-badge.current-badge {
                        background: #00B14F;
                        color: #FFFFFF;
                    }
                    
                    /* User POV Container & Radar Cone */
                    .user-pov-container {
                        position: relative;
                        width: 70px;
                        height: 70px;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        pointer-events: none;
                    }
                    .user-radar-cone {
                        position: absolute;
                        width: 64px;
                        height: 64px;
                        top: 3px;
                        left: 3px;
                        background: radial-gradient(circle at 50% 50%, rgba(0, 177, 79, 0.45) 0%, rgba(0, 177, 79, 0.15) 55%, rgba(0, 177, 79, 0) 70%);
                        clip-path: polygon(50% 50%, 15% 0%, 85% 0%);
                        transform-origin: 50% 50%;
                        transition: transform 0.15s ease-out;
                    }
                    .marker-user {
                        width: 32px;
                        height: 32px;
                        border-radius: 50%;
                        background: #FFFFFF;
                        border: 3.5px solid #00B14F;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        box-shadow: 0 4px 14px rgba(0,177,79,0.45);
                        z-index: 2;
                    }
                    .marker-user-dot {
                        width: 12px;
                        height: 12px;
                        border-radius: 50%;
                        background: #00B14F;
                    }

                    @keyframes pulse-ring {
                        0% { box-shadow: 0 0 0 0 rgba(0, 177, 79, 0.65); }
                        70% { box-shadow: 0 0 0 14px rgba(0, 177, 79, 0); }
                        100% { box-shadow: 0 0 0 0 rgba(0, 177, 79, 0); }
                    }
                    .goongjs-popup-content {
                        border-radius: 16px;
                        padding: 8px 10px;
                        box-shadow: 0 10px 25px rgba(0,0,0,0.18);
                    }
                    .poi-popup-card {
                        max-width: 220px;
                        padding: 4px;
                    }
                    .poi-popup-badge {
                        font-size: 9px;
                        font-weight: 800;
                        text-transform: uppercase;
                        color: #00B14F;
                        background: #E8F5E9;
                        display: inline-block;
                        padding: 2px 6px;
                        border-radius: 6px;
                        margin-bottom: 4px;
                    }
                    .poi-popup-title {
                        font-size: 13px;
                        font-weight: 800;
                        color: #1A1A1A;
                        margin-bottom: 6px;
                        line-height: 1.3;
                    }
                    .poi-popup-btn {
                        background: #00B14F;
                        color: #FFFFFF;
                        border: none;
                        border-radius: 8px;
                        padding: 6px 12px;
                        font-size: 11.5px;
                        font-weight: 700;
                        cursor: pointer;
                        width: 100%;
                    }
                </style>
            </head>
            <body>
                <div id="map"></div>
                <script>
                    var goongApiKey = '$goongApiKey';
                    var goongMaptilesKey = '$goongMaptilesKey';
                    var ndaMapsApiKey = '$ndaMapsApiKey';
                    var centerLat = $centerLat;
                    var centerLng = $centerLng;
                    var stops = $markersJson;
                    var userInitialLat = $activeUserLat;
                    var userInitialLng = $activeUserLng;
                    var currentHeading = $deviceHeading;

                    var activeMapKey = (goongMaptilesKey && goongMaptilesKey !== 'YOUR_GOONG_MAPTILES_KEY' && goongMaptilesKey.length > 5) ? goongMaptilesKey : goongApiKey;

                    var mapStyle = null;
                    if (ndaMapsApiKey && ndaMapsApiKey.length > 5) {
                        // NDAMaps official vector style
                        mapStyle = 'https://tiles.ndamaps.vn/styles/basic/style.json?apikey=' + ndaMapsApiKey;
                    } else if (activeMapKey && activeMapKey.length > 5 && activeMapKey !== 'YOUR_GOONG_API_KEY') {
                        // Goong vector style
                        mapStyle = 'https://tiles.goong.io/assets/goong_map_web.json?api_key=' + activeMapKey;
                    } else {
                        // Resilient high-res basemap style with Saigon landmarks
                        mapStyle = {
                            "version": 8,
                            "name": "VietnamAlleyMap",
                            "sources": {
                                "vietnam-tiles": {
                                    "type": "raster",
                                    "tiles": [
                                        "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                                    ],
                                    "tileSize": 256,
                                    "attribution": "© NDAMaps / Goong Maps"
                                }
                            },
                            "layers": [
                                {
                                    "id": "vietnam-tiles-layer",
                                    "type": "raster",
                                    "source": "vietnam-tiles",
                                    "minzoom": 0,
                                    "maxzoom": 20
                                }
                            ]
                        };
                    }

                    var map = new maplibregl.Map({
                        container: 'map',
                        style: mapStyle,
                        center: [centerLng, centerLat],
                        zoom: 15.5,
                        attributionControl: false
                    });

                    map.addControl(new maplibregl.AttributionControl({
                        compact: true,
                        customAttribution: '© <a href="https://ndamaps.vn" target="_blank">NDAMaps</a> / <a href="https://goong.io" target="_blank">Goong Maps</a>'
                    }), 'bottom-right');

                    var destinationMarkers = [];
                    var userMarker = null;
                    var isMapLoaded = false;
                    var pendingRouteCoords = null;

                    // Fallback to resilient style if remote style json has network or token error
                    var hasFallbackTriggered = false;
                    map.on('error', function(e) {
                        if (!hasFallbackTriggered && (!isMapLoaded || (e.error && e.error.status === 401 || e.error.status === 403 || e.error.status === 404))) {
                            hasFallbackTriggered = true;
                            map.setStyle({
                                "version": 8,
                                "name": "VietnamAlleyMapFallback",
                                "sources": {
                                    "vietnam-tiles": {
                                        "type": "raster",
                                        "tiles": [
                                            "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                                        ],
                                        "tileSize": 256,
                                        "attribution": "© NDAMaps / Goong Maps"
                                    }
                                },
                                "layers": [
                                    {
                                        "id": "vietnam-tiles-layer",
                                        "type": "raster",
                                        "source": "vietnam-tiles",
                                        "minzoom": 0,
                                        "maxzoom": 20
                                    }
                                ]
                            });
                        }
                    });

                    // Decodes Goong Directions overview_polyline points to [lng, lat]
                    function decodePolyline(encoded) {
                        var points = [];
                        var index = 0, len = encoded.length;
                        var lat = 0, lng = 0;
                        while (index < len) {
                            var b, shift = 0, result = 0;
                            do {
                                b = encoded.charCodeAt(index++) - 63;
                                result |= (b & 0x1f) << shift;
                                shift += 5;
                            } while (b >= 0x20);
                            var dlat = ((result & 1) ? ~(result >> 1) : (result >> 1));
                            lat += dlat;

                            shift = 0;
                            result = 0;
                            do {
                                b = encoded.charCodeAt(index++) - 63;
                                result |= (b & 0x1f) << shift;
                                shift += 5;
                            } while (b >= 0x20);
                            var dlng = ((result & 1) ? ~(result >> 1) : (result >> 1));
                            lng += dlng;

                            // GeoJSON coordinates [lng, lat]
                            points.push([lng * 1e-5, lat * 1e-5]);
                        }
                        return points;
                    }

                    map.on('load', function() {
                        isMapLoaded = true;

                        map.addSource('route', {
                            'type': 'geojson',
                            'data': {
                                'type': 'Feature',
                                'properties': {},
                                'geometry': {
                                    'type': 'LineString',
                                    'coordinates': []
                                }
                            }
                        });

                        // Glow layer
                        map.addLayer({
                            'id': 'route-glow',
                            'type': 'line',
                            'source': 'route',
                            'layout': { 'line-join': 'round', 'line-cap': 'round' },
                            'paint': {
                                'line-color': '#00B14F',
                                'line-width': 13,
                                'line-opacity': 0.3
                            }
                        });

                        // Casing border
                        map.addLayer({
                            'id': 'route-casing',
                            'type': 'line',
                            'source': 'route',
                            'layout': { 'line-join': 'round', 'line-cap': 'round' },
                            'paint': {
                                'line-color': '#144233',
                                'line-width': 7,
                                'line-opacity': 0.85
                            }
                        });

                        // Core polyline
                        map.addLayer({
                            'id': 'route-core',
                            'type': 'line',
                            'source': 'route',
                            'layout': { 'line-join': 'round', 'line-cap': 'round' },
                            'paint': {
                                'line-color': '#00D65F',
                                'line-width': 4.5,
                                'line-opacity': 1.0
                            }
                        });

                        if (pendingRouteCoords) {
                            window.updateRouteGeometry(pendingRouteCoords);
                        }
                    });

                    // Render Real-time Street Polyline on Goong Vector Map
                    window.updateRouteGeometry = function(lngLatArray) {
                        if (!isMapLoaded) {
                            pendingRouteCoords = lngLatArray;
                            return;
                        }

                        var coords = lngLatArray || [];
                        var routeSource = map.getSource('route');
                        if (routeSource) {
                            routeSource.setData({
                                'type': 'Feature',
                                'properties': {},
                                'geometry': {
                                    'type': 'LineString',
                                    'coordinates': coords
                                }
                            });
                        }
                    };

                    // Fetch Alley Route with Pedestrian Foot & Sidewalk Routing
                    window.fetchStreetRoute = function(waypoints) {
                        if (!waypoints || waypoints.length < 2) {
                            window.updateRouteGeometry([]);
                            return;
                        }

                        // 1. Primary: OSRM Pedestrian Foot Routing (Specialized for Saigon alleys & sidewalks)
                        var osrmCoords = waypoints.map(function(pt) {
                            return Number(pt[1]).toFixed(6) + ',' + Number(pt[0]).toFixed(6);
                        }).join(';');
                        
                        var osrmUrl = 'https://router.project-osrm.org/route/v1/foot/' + osrmCoords + '?overview=full&geometries=geojson&steps=false';
                        
                        fetch(osrmUrl)
                            .then(function(res) { return res.json(); })
                            .then(function(data) {
                                if (data && data.routes && data.routes.length > 0 && data.routes[0].geometry && data.routes[0].geometry.coordinates && data.routes[0].geometry.coordinates.length > 0) {
                                    window.updateRouteGeometry(data.routes[0].geometry.coordinates);
                                } else {
                                    fallbackToGoongRouting(waypoints);
                                }
                            })
                            .catch(function() {
                                fallbackToGoongRouting(waypoints);
                            });
                    };

                    function fallbackToGoongRouting(waypoints) {
                        var origin = waypoints[0]; // [lat, lng]
                        var destination = waypoints[waypoints.length - 1]; // [lat, lng]
                        var keyToUse = (goongApiKey && goongApiKey !== 'YOUR_GOONG_API_KEY' && goongApiKey.length > 5) ? goongApiKey : activeMapKey;

                        if (keyToUse && keyToUse.length > 5) {
                            var url = 'https://rsapi.goong.io/direction?origin=' + origin[0] + ',' + origin[1] + '&destination=' + destination[0] + ',' + destination[1] + '&vehicle=bike&api_key=' + keyToUse;
                            fetch(url)
                                .then(function(res) { return res.json(); })
                                .then(function(data) {
                                    if (data && data.routes && data.routes.length > 0 && data.routes[0].overview_polyline && data.routes[0].overview_polyline.points) {
                                        var decoded = decodePolyline(data.routes[0].overview_polyline.points);
                                        window.updateRouteGeometry(decoded);
                                    } else {
                                        var straightLine = waypoints.map(function(pt) { return [pt[1], pt[0]]; });
                                        window.updateRouteGeometry(straightLine);
                                    }
                                })
                                .catch(function() {
                                    var straightLine = waypoints.map(function(pt) { return [pt[1], pt[0]]; });
                                    window.updateRouteGeometry(straightLine);
                                });
                        } else {
                            var straightLine = waypoints.map(function(pt) { return [pt[1], pt[0]]; });
                            window.updateRouteGeometry(straightLine);
                        }
                    }

                    window.renderStops = function(stopsList) {
                        destinationMarkers.forEach(function(m) { m.remove(); });
                        destinationMarkers = [];

                        if (!stopsList || stopsList.length === 0) return;

                        stopsList.forEach(function(stop) {
                            var iconSymbol = stop.symbol || '📍';
                            var statusClass = 'locked';
                            var badgeClass = '';
                            if (stop.status === 'COMPLETED') {
                                statusClass = 'completed';
                                iconSymbol = '✅';
                                badgeClass = 'completed-badge';
                            } else if (stop.status === 'CURRENT') {
                                statusClass = 'current';
                                iconSymbol = '🏃';
                                badgeClass = 'current-badge';
                            }

                            var el = document.createElement('div');
                            el.className = 'marker-stop-wrapper';
                            el.innerHTML = '<div class="marker-stop ' + statusClass + '">' + iconSymbol + '</div>' +
                                           '<div class="marker-number-badge ' + badgeClass + '">' + stop.num + '</div>';

                            var popupHtml = '<div class="poi-popup-card">' +
                                '<div class="poi-popup-badge">' + (stop.status === 'COMPLETED' ? 'Đã hoàn thành' : (stop.status === 'CURRENT' ? 'Đang hướng đến' : stop.category)) + '</div>' +
                                '<div class="poi-popup-title">Chặng ' + stop.num + ': ' + stop.name + '</div>' +
                                '<button class="poi-popup-btn" onclick="window.AndroidBridge && window.AndroidBridge.selectStop(\'' + stop.id + '\')">Khám phá chặng này</button>' +
                                '</div>';

                            var popup = new maplibregl.Popup({ offset: 25, closeButton: false })
                                .setHTML(popupHtml);

                            el.addEventListener('click', function(e) {
                                e.stopPropagation();
                                if (window.AndroidBridge && window.AndroidBridge.selectStop) {
                                    window.AndroidBridge.selectStop(stop.id);
                                }
                            });

                            var marker = new maplibregl.Marker({ element: el })
                                .setLngLat([stop.lng, stop.lat])
                                .setPopup(popup)
                                .addTo(map);

                            destinationMarkers.push(marker);
                        });
                    };

                    // User Marker with POV Radar Heading Cone in Vector Map
                    window.updateUserLocation = function(lat, lng, heading) {
                        var rot = (typeof heading === 'number') ? heading : 0;
                        if (!userMarker) {
                            var userEl = document.createElement('div');
                            userEl.className = 'custom-user-pov-marker';
                            userEl.innerHTML = '<div class="user-pov-container">' +
                                               '<div class="user-radar-cone" id="radarCone" style="transform: rotate(' + rot + 'deg);"></div>' +
                                               '<div class="marker-user"><div class="marker-user-dot"></div></div>' +
                                               '</div>';
                            userMarker = new maplibregl.Marker({ element: userEl })
                                .setLngLat([lng, lat])
                                .addTo(map);
                        } else {
                            userMarker.setLngLat([lng, lat]);
                            var cone = document.getElementById('radarCone');
                            if (cone) {
                                cone.style.transform = 'rotate(' + rot + 'deg)';
                            }
                        }
                    };

                    window.updateHeading = function(heading) {
                        var cone = document.getElementById('radarCone');
                        if (cone) {
                            cone.style.transform = 'rotate(' + heading + 'deg)';
                        }
                    };

                    window.centerOnUser = function(lat, lng, zoomLevel) {
                        var zoom = zoomLevel || 17.5;
                        map.flyTo({
                            center: [lng, lat],
                            zoom: zoom,
                            speed: 1.5,
                            curve: 1,
                            essential: true
                        });
                    };

                    window.focusUserAndTarget = function(userLat, userLng, targetLat, targetLng) {
                        var bounds = new maplibregl.LngLatBounds(
                            [Math.min(userLng, targetLng), Math.min(userLat, targetLat)],
                            [Math.max(userLng, targetLng), Math.max(userLat, targetLat)]
                        );
                        map.fitBounds(bounds, {
                            padding: { top: 90, bottom: 90, left: 60, right: 60 },
                            maxZoom: 17
                        });
                    };

                    // Initial draw
                    window.updateUserLocation(userInitialLat, userInitialLng, currentHeading);
                    window.renderStops(stops);

                    var initialWaypoints = $lineCoordinatesJson;
                    if (initialWaypoints && initialWaypoints.length >= 2) {
                        window.fetchStreetRoute(initialWaypoints);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()

        val lastQuestId = remember { arrayOf<String?>(null) }
        val lastStopsJson = remember { arrayOf("") }
        val lastWaypointsJson = remember { arrayOf("") }
        val lastUserLocStr = remember { arrayOf("") }
        val lastHeading = remember { arrayOf(-1f) }

        val jsBridge = remember {
            GoongJsBridge { stopId -> }
        }
        jsBridge.onStopSelected = { stopId ->
            quest?.stops?.find { it.id == stopId }?.let { onSelectStop(it) }
        }

        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    layoutParams = android.view.ViewGroup.LayoutParams(
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                        android.view.ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    webChromeClient = object : WebChromeClient() {
                        override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                            return true
                        }
                    }
                    webViewClient = object : WebViewClient() {
                        override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                            view?.destroy()
                            return true
                        }
                    }
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.databaseEnabled = true
                    settings.cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    addJavascriptInterface(jsBridge, "AndroidBridge")
                    loadDataWithBaseURL("https://tiles.goong.io/", htmlContent, "text/html", "UTF-8", null)
                }
            },
            update = { webView ->
                if (quest?.id != lastQuestId[0]) {
                    lastQuestId[0] = quest?.id
                    lastStopsJson[0] = ""
                    lastWaypointsJson[0] = ""
                    lastUserLocStr[0] = ""
                    webView.loadDataWithBaseURL("https://tiles.goong.io/", htmlContent, "text/html", "UTF-8", null)
                }

                // Update stops dynamically without full reload
                if (markersJson != lastStopsJson[0]) {
                    lastStopsJson[0] = markersJson
                    webView.evaluateJavascript("if (window.renderStops) { window.renderStops($markersJson); }", null)
                }

                // Update User location & POV
                val userLocStr = "$activeUserLat,$activeUserLng"
                if (userLocStr != lastUserLocStr[0]) {
                    lastUserLocStr[0] = userLocStr
                    webView.evaluateJavascript("if (window.updateUserLocation) { window.updateUserLocation($activeUserLat, $activeUserLng, $deviceHeading); }", null)
                    
                    if (isPovFollowMode) {
                        webView.evaluateJavascript("if (window.centerOnUser) { window.centerOnUser($activeUserLat, $activeUserLng, 18); }", null)
                    }
                }

                // Update Heading / Compass Angle smoothly
                if (Math.abs(deviceHeading - lastHeading[0]) > 2f) {
                    lastHeading[0] = deviceHeading
                    webView.evaluateJavascript("if (window.updateHeading) { window.updateHeading($deviceHeading); }", null)
                }

                if (shouldCenter[0]) {
                    webView.evaluateJavascript("if (window.centerOnUser) { window.centerOnUser($activeUserLat, $activeUserLng, 18); }", null)
                    shouldCenter[0] = false
                }

                if (shouldFitRoute[0] && ongoingStop != null) {
                    webView.evaluateJavascript("if (window.focusUserAndTarget) { window.focusUserAndTarget($activeUserLat, $activeUserLng, ${ongoingStop.latitude}, ${ongoingStop.longitude}); }", null)
                    shouldFitRoute[0] = false
                }

                // Update route waypoints
                val currentWaypointsJson = if (isJourneyStarted && quest != null && ongoingStop != null) {
                    // In active navigation: Route directly from user's live GPS position to the target checkpoint
                    "[[ $activeUserLat, $activeUserLng ], [ ${ongoingStop.latitude}, ${ongoingStop.longitude} ]]"
                } else if (quest != null && quest.stops.size >= 2) {
                    // In preview mode: Connect all quest checkpoints sequentially along pedestrian walkways
                    quest.stops.joinToString(prefix = "[", postfix = "]", separator = ", ") {
                        "[ ${it.latitude}, ${it.longitude} ]"
                    }
                } else {
                    "[]"
                }

                if (currentWaypointsJson != lastWaypointsJson[0]) {
                    lastWaypointsJson[0] = currentWaypointsJson
                    if (currentWaypointsJson == "[]") {
                        webView.evaluateJavascript("if (window.updateRouteGeometry) { window.updateRouteGeometry([]); }", null)
                    } else {
                        webView.evaluateJavascript("if (window.fetchStreetRoute) { window.fetchStreetRoute($currentWaypointsJson); }", null)
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Floating Action Controls on right side of Map
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Google Maps native navigation launcher
            if (isJourneyStarted && ongoingStop != null) {
                FloatingActionButton(
                    onClick = {
                        val gmmIntentUri = Uri.parse("google.navigation:q=${ongoingStop.latitude},${ongoingStop.longitude}&mode=w")
                        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                            setPackage("com.google.android.apps.maps")
                        }
                        try {
                            context.startActivity(mapIntent)
                        } catch (e: Exception) {
                            val fallbackUri = Uri.parse("geo:${ongoingStop.latitude},${ongoingStop.longitude}?q=${ongoingStop.latitude},${ongoingStop.longitude}(${Uri.encode(ongoingStop.name)})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri))
                        }
                    },
                    modifier = Modifier.size(46.dp),
                    containerColor = PaperWhite,
                    contentColor = Color(0xFFEA4335)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Open in Google Maps",
                        tint = Color(0xFFEA4335),
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // POV Follow Mode Toggle (Compass & User Heading Tracking)
            FloatingActionButton(
                onClick = { 
                    isPovFollowMode = !isPovFollowMode
                    if (isPovFollowMode) {
                        shouldCenter[0] = true
                    }
                },
                modifier = Modifier.size(46.dp),
                containerColor = if (isPovFollowMode) GrabGreen else PaperWhite,
                contentColor = if (isPovFollowMode) Color.White else ForestGreen
            ) {
                Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "POV Compass Mode",
                    modifier = Modifier
                        .size(22.dp)
                        .rotate(if (isPovFollowMode) deviceHeading else 0f)
                )
            }

            // Fit Route Button (Overview of User & Target Stop)
            FloatingActionButton(
                onClick = { 
                    isPovFollowMode = false
                    shouldFitRoute[0] = true 
                },
                modifier = Modifier.size(46.dp),
                containerColor = PaperWhite,
                contentColor = ForestGreen
            ) {
                Icon(
                    imageVector = Icons.Default.Navigation,
                    contentDescription = "Fit Route Overview",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Center on user location
            FloatingActionButton(
                onClick = { 
                    isPovFollowMode = true
                    shouldCenter[0] = true 
                },
                modifier = Modifier.size(46.dp),
                containerColor = PaperWhite,
                contentColor = GrabGreen
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on My Location",
                    modifier = Modifier.size(20.dp)
                )
            }

            // Simulate walk towards ongoing destination (for testing & demo)
            if (onSimulateStep != null && quest != null) {
                FloatingActionButton(
                    onClick = onSimulateStep,
                    modifier = Modifier.size(46.dp),
                    containerColor = ClayOrange,
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsWalk,
                        contentDescription = "Simulate Walk Step",
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
