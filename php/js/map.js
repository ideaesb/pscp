

function postStationsLoaded() {
    if (queryState.station) {
        var station = $("#scrollArea .station:contains(" + queryState.station + ")");
        if (station.length) {
            var idx = station.parent().children().index(station.get(0));
            var marker = marray[idx];
            
            $.get(marker.infoURL,function(d) {
                var infoDiv = $("<div id='infoTab'></div>");
                infoDiv.html(d);
                $("a",infoDiv).click(openInNewWindow);
                $("<a title='Enter product viewing mode' href=''>[ View Product ]</a>").click(function() {
                    pushProductState({products:true});
                    return false;
                }).prependTo(infoDiv); 
                
                marker.openInfoWindowTabs([
                    new GInfoWindowTab('Station Info',infoDiv.get(0))
                ]);
            });
            highliteStation(idx);
        }
        checkProductState(queryState);
    }
}

function showProducts(productURL) {
    if (productURL) {
        var pargs = {
                typeid : queryState.group,
                thumbs: true
            };
        $.get(productURL,pargs,function(d) {
            $("#products").html(d).find("a").eq(0).click(openInNewWindow);
            if (!$("#products").is(":visible")) {
                $("#showToggle").click();
            }
            $(".productLink a").click(function() {
                var href = $(this).attr("href"),data;
                data = unparam(href.split("?")[1]);
                pushProductState({
                   products: true,
                   pid: data.img
                });
                return false;
            });
        });
    } else {
        $("#products").html("<p style='margin-top:10px'>Select a station on the right to view the products.</p>");
    }
}

function setStationQuery(quality, process, region, group) {
    var title, data = {
    	quality: quality,
        process: process,
        typeid : group,
        region : region,
        indicator: ''
    };
    title = process ? processDisplay[process] + " " : "";
    if (group != null) {
        title += $("input[value=1][name=type]").parent().text() + " ";
    }
    title += "Insitu Platforms ";
    if (region) {
        title += "in the " + regionDisplay[region];
    }
    if (quality) {
    	title += " With Data Quality Level " + quality;
    }
    $("#maptitle").text(title);
    marray = [];
    map.clearOverlays();
    $("#scrollArea").html("Loading Stations");
    $.get(pscpAPI + "/stations/locations.js",data,function(d) {
        stationsLoaded(d);
    },"json");
}

function stateChange(hash) {
    var args = unparam(hash.replace("#","")),empty = true;
    function check(inp,val) {
        val = val || "";
        var el = $("input[name=" + inp + "][value=" + val + "]");
        el.length && (el.get(0).checked = true);
    }
    for (var a in args) {
        empty = false;break;
    }
    if (empty) {
        args.process = "";
        args.region = "";
        args.quality = "";
    }
    function update(name) {
        if (args[name]) check(name,args[name]);
    }
    if (args.group != queryState.group ||
        args.process != queryState.process ||
        args.region != queryState.region ||
        args.quality != queryState.quality) {
        if (typeof args.group == 'undefined') {
            $("#selectaproducttype").css({
                background: 'red',
                color: 'white'
            });
            return;
        }
        $("#selectaproducttype").css({
            color: '',
            background: ''
        });
        update('quality');
        update('process');
        update('region');
        update('type');
        queryState = args;
        setStationQuery(args.quality, args.process, args.region, args.group);
    }
    if (args.station != queryState.station ||
        args.products != queryState.products ||
        args.pid != queryState.pid) {
        queryState = args;
        postStationsLoaded();
    }
}

function checkProductState(args) {
    var productToggle = $("#showToggle");
    var productOverlay = $("#productoverlay");
    if (args.products) {
        var idx = getStationIndex(args.station);
        if (idx >= 0) {
            showProducts(marray[idx].productURL);
        }
        productOverlay.width($("#map").width());
        productOverlay.fadeIn(500);
        productToggle.text("[ View Map ]");
    } else {
        productOverlay.fadeOut(500);
        productToggle.text("[ View Products ]");
    }
    if (typeof args.products != "undefined") {
        queryState.products = args.products;
    }
    if (args.pid) {
        var link = productOverlay.find("a[href*=" + args.pid + "]");
        if (link.length) {
            $("#imageViewer img").attr({
                src:link.attr('href')
            });
            $("#imageViewer").show(500);
        }
    } else {
        $("#imageViewer").hide(500);
    }
}

function pushProductState(args) {
    args = args || {};
    var data = unparam(window.location.hash.replace("#",""));
    function update(key) { args[key] && (data[key] = args[key]) || delete data[key]; }
    update('products');
    update('pid');
    window.location.hash = $.param(data);
}


function pushQueryState() {
    var data = {},quality, process, region, group;
    function push(key,val) { val && (data[key] = val); }
    process = $("input[name=process]:checked").val();
    region = $("input[name=region]:checked").val();
    quality = $("input[name=quality]:checked").val();
    group = $("#selectType").val();
    if (group && $("#helpoverlay:visible").length) {
        $("#helpoverlay").fadeOut(500);
    }

    push("quality", quality);
    push("process", process);
    push("region", region);
    push("group", group);
    push("products",queryState.products);
    push("station",queryState.station);
    window.location.hash = $.param(data);
}

$(function() {
    initMap();
    if ($.browser.msie) {
        // force IE to fire change on click
        // problem is that change not fired normally until focus is changed
        $('input:radio').click(function() {
            this.blur();
            this.focus();
        });
        $("#selectType").change(function() {
            this.blur();
            this.focus();
        })
    }
    $("input[name=process]").change(pushQueryState);
    $("input[name=region]").change(pushQueryState);
    $("input[name=quality]").change(pushQueryState);
    $("select[name=type]").change(pushQueryState);
    $("#help").click(function() {
        var overlay = $("#helpoverlay");
        overlay.width($("#map").width());
        if (overlay.is(":hidden")) {
            $(this).text("[ Hide Help ]");
            overlay.fadeIn(250);
        } else {
            $(this).text("[ Help ]");
            overlay.fadeOut(250);
        }
    });
    $("#scrollArea").click(function(ev) {
        var tgt = ev.target;
        if (tgt && $(tgt).hasClass("station")) {
            var idx = $(tgt).parent().children().index(tgt);
            GEvent.trigger(marray[idx],"click");
        }
    });
    $("#showToggle").click(function() {
        var productOverlay = $("#productoverlay");
        var args = {};
        if (!productOverlay.is(":visible")) {
            args["products"] = true;
        }
        pushProductState(args);
        return false;
    });
    $("#imageViewer").click(function(d) {
        pushProductState({products:true});
    });
    $("#aboutViewer").click(function(d) {
        $("#aboutViewer").fadeOut(500);
    });
    $(window).history(function( e, ui ) {
        if (ui) stateChange(ui.value);
    });
    if (!window.location.hash) {
        $("#help").click();
    }
    stateChange(window.location.hash.replace("#",""));
});
window.onunload = GUnload;
