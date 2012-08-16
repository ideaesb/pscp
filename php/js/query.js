$(function() {
    $(".choices li").hover(function() {
        $(this).css('text-decoration','underline');
    }, function() {
        $(this).css('text-decoration','');
    });

    var inputs = ["inputProcessIndicator","inputProductType"];
    function checkSelection() {
        var enabled = true, query = $("#submitQuery");
        $.each(inputs,function(i,e) {
            enabled &= e.is(".selected");
        });
        enabled &= $(".maplabel").is(".selected");
        if (enabled) query.removeAttr("disabled");
        else query.attr("disabled","true");
    }
    function loadhash() {
        var hash = window.location.hash;
        if (hash.length) {
            var params = unparam(hash.substr(1)),p;
            for (p in params) {
                if (p == "indicator") {
                    $("#I" + (typeof params[p] == "string" ? params[p] : params[p].join("-"))).click();
                } else if (p == "region") {
                    $("#R" + params[p]).click();
                } else if (p == "typeid") {
                    p = params[p];
                    $("#T" + p[0] + (p.length > 1 ? "-" + p[p.length - 1] : "")).click() ;
                }
            }
        }
    }
    $.each(inputs,function(i,e) {
        var choices = $("#" + e + " .choices li");
        inputs[i] = choices;
        choices.click(function() {
            choices.removeClass("selected");
            $(this).addClass("selected");
            checkSelection();
        });
    });
    $(".maplabel").click(function() {
        $(".maplabel").removeClass("selected");
        $(this).addClass("selected");
        checkSelection();
        return false;
    });
    $("#submitQuery").click(function() {
        var typeids = [];
        var selected = [];
        var idspec = $("#inputProductType .selected").attr('id');
        selected.push(idspec);
        idspec = idspec.substr(1).split("-");
        if (idspec.length > 1) {
            for (var i = new Number(idspec[0]), ii = new Number(idspec[1]); i <= ii; i++) {
                typeids.push(i);
            }
        } else {
            typeids = idspec;
        }
        var indicspec = $("#inputProcessIndicator .selected").attr('id');
        selected.push(indicspec);
        indicspec = indicspec.substr(1).replace("_"," ").split("-");
        var regionspec = $(".maplabel.selected").attr('id');
        selected.push(regionspec);
        var processspec = $("#inputProcessIndicator .selected").parent().prev().attr('id');
        window.location = "index.php?page=results#" + $.param({
            region : regionspec.substr(1),
            process : processspec.substr(1),
            typeid : typeids,
            indicator : indicspec
        },true);
    });
    checkSelection();
    loadhash();
});
