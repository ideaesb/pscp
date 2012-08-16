<?php $page_title = "Library"; ?>
<style type="text/css">
    #library {
        overflow:auto;
        height: 700px;
    }
    #library th {
        background: #F0F8FE;
        border-bottom: 1px solid #AEDBF4;
        text-align: left;
    }
    #library tr {
        text-align: left;
    }
    .h {
        background: yellow;
    }
</style>
<div id="contentArea">
    <h1>Pacific Storms Climatology Library</h1>
    <div class="content">
        Search : <input id="search" type="text"/>
        <div id="library">
        </div>
    </div>
</div>
<script type="text/javascript">
    pageload = function() {
        var handle;
        $("#library").html("Loading library ...").click(function(ev) {
            var href = $(ev.srcElement || ev.originalTarget).attr('href');
            href && window.open(href,"stationpage");
            return false;
        });
        $.get(pscpAPI + "/library",function(d) {
            var table = $(d);
            $("tr:even",table).css({
                background:"#F0F8FE",
                width:"100%"
            });
            $("#library").html("").append(table);
        });
        function search() {
            var val = $("#search").val();
            var rows = $("#library tbody tr");
            $("td .h").each(function(i,e) {
                e = $(e).parent();
                e.html(e.text());
            });
            if (val == "") {
                rows.css("display","");
            } else {
                var re = new RegExp(val,"g" + (/[A-Z]/.exec(val) ? "" : "i"));
                rows.each(function() {
                    var cells = $("td",this);
                    var hide = true;
                    for (var c = 0; c < cells.length; c++) {
                        if (re.test(cells.eq(c).text())) {
                            cells.eq(c).html(cells.eq(c).html().replace(re,function(s) { return "<span class='h'>" + s + "</span>"}));
                            hide = false; break;
                        }
                    }
                    $(this).css("display",hide ? "none" : "");
                });
            }
        }
        $("#search").keyup(function() {
            if (handle) window.clearTimeout(handle);
            handle = window.setTimeout(search, 500);
        });
    };
</script>