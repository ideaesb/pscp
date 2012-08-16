
function setStationQuery(args) {
    $("#regions li").removeClass("selected");
    $("#" + args.region).addClass("selected");
    marray = [];
    map.clearOverlays();
    $("#scrollArea").html("Loading Stations");
    $.get(pscpAPI + "/stations/locations.js",$.param(args,true),function(d) {
        stationsLoaded(d);
    },"json");
}

function stateChange(hash) {
    var args = unparam(hash.replace("#","")),empty = true;
    for (var a in args) {
        empty = false;break;
    }
    if (empty) {
        window.location = "index.php?page=query";
        return;
    }
    if (args.region != queryState.region) {
        queryState = args;
        setStationQuery(args);
    }
    if (args.station && args.station != queryState.station) {
        queryState = args;
        postStationsLoaded();
    }
    queryState = args;
}

function pushQueryState(args) {
    var data = $.extend({},queryState);
    function push(key,val) {
        val && (data[key] = val);
    }
    delete data.station;
    push("region",args.region);
    window.location.hash = $.param(data,true);
}

$(function() {
    var contentWidth = $("#contentArea").width();
    var rightArea = $("#rightArea");
    var cw = contentWidth - rightArea.width() - 20 // 2 * padding;
    $("#map, #centerArea, #content").width(cw);
    rightArea.css("left",cw + 20);
    initMap();
    $("#scrollArea").click(function(ev) {
        var tgt = ev.target;
        if (tgt && $(tgt).hasClass("station")) {
            var idx = $(tgt).parent().children().index(tgt);
            // generate a click event on the related marker
            GEvent.trigger(marray[idx],"click", "stations");
        }
    });
    $("#regions li").click(function() {
        pushQueryState({region: $(this).attr('id')});
    });
    $(window).history(function( e, ui ) {
        if (ui) stateChange(ui.value);
    });
    stateChange(window.location.hash.replace("#",""));
});
window.onunload = GUnload;
