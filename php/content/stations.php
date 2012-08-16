<?php $page_title = "Stations"; ?>
<style type="text/css">
    #stations {
        overflow:auto;
        height: 700px;
    }
    #stations th {
        background: #F0F8FE;
        border-bottom: 1px solid #AEDBF4;
        text-align: left;
    }
    #stations tr {
        text-align: left;
    }
</style>
<div id="contentArea">
    <h1>Pacific Storms Climatology Stations</h1>
    <div class="content">

    <h2>Stations Listing</h2>
    <table class="filterTable"> 
        <tr>
            <td>
                <div class="smallLabel">Text Search:</div>
                <input id="search" type="text"/>
            </td>
            <td>
                <div class="smallLabel">Region:</div>
                <select id="regions">
                    <option value="">All Regions</option>
                    <option value="NNP">Northern North Pacific</option>
                    <option value="ENP">Eastern North Pacific</option>
                    <option value="CNP">Central North Pacific</option>
                    <option value="WNP">Western North Pacific</option>
                    <option value="CSP">Central South Pacific</option>
                    <option value="ESP">Eastern South Pacific</option>
                </select>
            </td>
            <td>
                <div class="smallLabel">&nbsp;</div>
                <input type="button" value="Reset" class="smallButton" title="Reset the search terms to their starting values." onclick="resetSearch()">
            </td>
        </tr>
    </table>
    <div id="stations">
    </div>
    </div>
</div>
<script type="text/javascript">
    
    var regionState = "";
    var textState = "";
    
    function resetSearch() {
        document.getElementById("search").value = "";
        document.getElementById("regions").selectedIndex = 0;
        regionState = "";
        textState = "";
        redraw();
    }
    
    function redraw() {
        var rows = $("#stations tbody tr");
        var re = new RegExp(textState,"i");
        rows.each(function() {
            var cells = $("td",this);
            var hide = true;
            
            for (var c = 0; c < cells.length; c++) {
                if (re.test(cells.eq(c).text())) {
                    hide = false; break;
                }
            }
            var regionTest = (regionState==""?cells[2].innerHTML:regionState);
            if (!hide && cells[2].innerHTML!=regionTest) {
                hide = true;
            }
            $(this).css("display",hide ? "none" : "");
        });
    }
    
    pageload = function() {
        $("#stations").html("Loading Stations ...").click(function(ev) {
            var href = $(ev.srcElement || ev.originalTarget).attr('href');
            href && window.open(href,"stationpage");
            return false;
        });
        $.get(pscpAPI + "/stations/overview", function(d) {
            $("#stations").html(d).find("table").css("width","100%").find("tr").hover(function() {
                $(this).css('background','#F0F8FE');
            },function() {
                $(this).css('background',null);
            }); 
        });
        $("#search").keyup(function() {
            textState = $(this).val();
            redraw();
        });
        $("#regions").click(function() {
            regionState = document.getElementById("regions").options[document.getElementById("regions").selectedIndex].value;
            redraw();
        });
    };
</script>
