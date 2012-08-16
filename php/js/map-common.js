var map,
baseIcon,
marray = [],
mapSettings = {
    CNP: {
        lat:15.111,
        lon:-166.208,
        zoom:4
    },
    CSP: {
        lat:-21.1383,
        lon: 178.423566,
        zoom:3
    },
    ENP: {
        lat:20.949,
        lon: -100.038,
        zoom:4
    },
    NNP: {
        lat:56.073,
        lon: -149.486,
        zoom:3
    },
    WNP: {
        lat:22.551,
        lon: 150.811,
        zoom:4
    },
    ESP: {
        lat:-20.7,
        lon: -95.6,
        zoom:3
    }
},
icons = {
    "HSsea level station" : "images/sls.png",
    "HSwave buoy" : "images/buoy.png",
    "HRmet station" : "images/rain.png",
    "SWmet station" : "images/wind.png",
    HS: "images/buoy.png",
    HR: "images/rain.png",
    SW: "images/wind.png"
},qualityDisplay = {
    1: "Level 1",
    2: "Level 2",
    3: "Level 3"
},processDisplay = {
    SW : "Strong Winds",
    HR : "Heavy Rains",
    HS : "High Seas"
},regionDisplay = {
    CNP: "Central North Pacific",
    CSP: "Central South Pacific",
    ENP: "Eastern North Pacific",
    NNP: "Northern North Pacific",
    WNP: "Western North Pacific"
},queryState = {};

function showProducts() {}

function initMap() {
    if (GBrowserIsCompatible()) {
        map = new GMap2(document.getElementById("map"));
        map.addControl(new GLargeMapControl());
        map.addControl(new GMapTypeControl());
        map.addMapType(G_HYBRID_MAP);
        var mapType = cookie("mapType");
        if (mapType) {
            var types = map.getMapTypes();
            for (var p in types) {
                var c = types[p];
                if (c.getName() == mapType) {
                    map.setMapType(c);
                    break;
                }
            }
        }
        GEvent.addListener(map.getInfoWindow(),"closeclick",function() {
            selectStation(-1);
        });
        GEvent.addListener(map, "maptypechanged", function() {
            cookie("mapType",map.getCurrentMapType().getName());
        });
    } else {
        alert("Sorry, Google Maps is not compatible with your current browser (settings)");
    }
}

function cookie(name,val) {
    if (typeof val == 'undefined') {
        if (document.cookie) {
            var parts = document.cookie.split(";");
            for (var i in parts) {
                var part = $.trim(parts[i]);
                if (part.substr(0, name.length + 1) === (name + '=')) {
                    return decodeURIComponent(part.substr(name.length + 1));
                }
            }
        }
    } else {
        var expires = 24 * 30 * 60 * 60 * 1000;
        if (val === null) {
            val = '';
            expires = -1;
        }
        var date = new Date();
        date.setTime(date.getTime() + expires);
        expires = "; expires=" + date.toUTCString();
        document.cookie = [name, '=', encodeURIComponent(val), expires].join('');
    }
}

function setMapLocation(loc) {
    var settings = mapSettings[loc];
    map.setCenter(new GLatLng(settings.lat,settings.lon), settings.zoom);
}

function openInNewWindow() {
    window.open($(this).attr('href'),"metadata");
    return false;
}

function createMarker(latitude, longitude, IconImage, infoURL, productURL, name) {
    var point  = new GLatLng(latitude,longitude);
    var baseIcon = new GIcon();
    baseIcon.iconSize = new GSize(11, 18);
    baseIcon.iconAnchor = new GPoint(5, 17);
    baseIcon.infoWindowAnchor = new GPoint(9, 2);
    var myIcon = new GIcon(baseIcon, IconImage);
    var marker = new GMarker(point, {
        icon: myIcon,
        title: name
    });
    marker.idx = marray.length;
    marker.infoURL = infoURL;
    marker.productURL = productURL;
    GEvent.addListener(marker, "click", markerClick);
    map.addOverlay(marker);
    marray.push(marker);
    return marker;
}

function markerClick(source) {
    var marker = this;
    if (source && source=="stations") {
        selectStation(marker.idx);
    }
    else {
        selectStation(marker.idx, "map");
    }
}

function highliteStation(idx) {
    var $scrollArea = $("#scrollArea"), top, viewTop, viewBottom;
    if (idx < 0) {
        $("div",$scrollArea).removeClass("selected-station");
    } else {
        var $station = $("div",$scrollArea).eq(idx);
        if (!$station.hasClass("selected-station")) {
            $("div",$scrollArea).removeClass("selected-station");
            $station.addClass("selected-station");
            top = $station.position().top;
            viewTop = $scrollArea.scrollTop() - $scrollArea.offset().top;
            viewBottom = $scrollArea.height() + viewTop;
            if (top < viewTop || top > viewBottom) {
                $scrollArea.scrollTop(top + $scrollArea.scrollTop() - $scrollArea.offset().top);
            }
        }
    }
}

function selectStation(idx, clickSource) {
    highliteStation(idx);
    pushStationState(clickSource);
}

function pushStationState(clickSource) {
    var data = unparam(window.location.hash.replace("#",""));
    var station = $("#scrollArea .selected-station").text();
    if (station) {
        data.station = station;
        if (clickSource) {
            data.source = clickSource
        }
        else {
            delete data.source;
        }
    } else {
        delete data.station;
        delete data.source;
    }
  window.location.hash = $.param(data,true);
}

function setStations(stations) {
    $("#stationsCount").text(stations.length);
    if (stations.length == 0) {
        $("#scrollArea").html("<b>No stations found</b>");
        return;
    }
    var list = $("#scrollArea");
    var html = [];
    for (var s in stations) {
        s = stations[s];
        var img = icons[s.process + s.stationclass] || icons[s.process] || "images/other.png";
        html.push("<div class='station'><img src='" + img + "'></img>" + s.stationname + "</div>");
        createMarker(s.latitude,s.longitude,img,s.info,s.products,s.stationname);
    }
    list.html(html.join(""));
}

function stationsLoaded(stations) {
    showProducts();
    setStations(stations);
    if (stations.length < 1 && queryState.region) {
        var settings = mapSettings[queryState.region];
        settings && map.setCenter(new GLatLng(settings.lat,settings.lon),settings.zoom);
    } else if (stations.length > 0) {
        var coords = [], bounds, zoomLevel, s;
        for (s in stations) {
            coords.push(new GLatLng(stations[s].latitude,stations[s].longitude));
        }
        bounds = new GPolyline(coords).getBounds();
        zoomLevel = map.getBoundsZoomLevel(bounds);
        map.setCenter(bounds.getCenter(), Math.min(zoomLevel,5));
    }
    postStationsLoaded();
}

function getStationIndex(name) {
    var idx = -1;
    if (name) {
        var station = $("#scrollArea .station:contains(" + queryState.station + ")");
        if (station.length) {
            idx = station.parent().children().index(station.get(0));
        }
    }
    return idx;
}

function postStationsLoaded() {
    if (queryState.station) {
        var idx = getStationIndex(queryState.station);
        if (idx >= 0) {
            var marker = marray[idx];
            $.get(marker.infoURL,function(d) {
                var infoDiv = $("<div id='infoTab'></div>");
                infoDiv.html(d);
                $("a",infoDiv).click(openInNewWindow);
                var productDiv = $("<div id='productTab'>Loading ...</div>");
                marker.openInfoWindowTabs([
                    new GInfoWindowTab('Station/Data',infoDiv.get(0)),
                    new GInfoWindowTab('Products',productDiv.get(0))
                ], {selectedTab: (queryState.source == "map" ? 1:0)});
                var pargs = {
                    version : 2,
                    typeid : queryState.typeid,
                    indicator: queryState.indicator
                };
                $.get(marker.productURL,pargs,function(d) {
                    productDiv.html(d);
                });
            });
            highliteStation(idx);
        }
    }
}

function unparam(value) {
    var params = {},
    pieces = value.split('&');
    for (var i = 0, l = pieces.length; i < l; i++) {
        var pair = pieces[i].split('=', 2);
        var key = decodeURIComponent(pair[0]);
        var val = pair.length == 2 ?
            decodeURIComponent(pair[1].replace(/\+/g, ' ')) : true;
        if (key in params) {
            var old = params[key];
            if (typeof old == "string") {
                params[key] = old = [old];
            }
            old.push(val);
        } else {
            params[key] = val;
        }
    }
    return params;
}