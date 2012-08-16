<?php
require ('inc/db.php' );
$page_title = "Query Results";
?>
<style type="text/css">
    @import url(css/results.css);
</style>
<div id="contentArea">
    
    <div id="centerArea">
        <div class="helpText">
            Select an individual station icon to reveal a <i>station/data</i> 
            box that contains basic information about the station and data, as 
            well as a <i>products</i> box that lists products associated with 
            the identified Product Type. To view a specific product for a 
            station, simply select it.
        </div>
        <div class="content">
            <ul id="regions">
                <li id="NNP" title="Northern North Pacific">Northern North Pacific</li>
                <li id="WNP" title="Western North Pacific">Western North Pacific</li>
                <li id="CNP" title="Central North Pacific">Central North Pacific</li>
                <li id="ENP" title="Eastern North Pacific">Eastern North Pacific</li>
                <li id="CSP" title="Central South Pacific">Central South Pacific</li>
                <li id="ESP" title="Eastern South Pacific">Eastern South Pacific</li>
            </ul>
            <div id="map"></div>
        </div>
    </div>
    <div id="rightArea">
        <div class="title">Stations - <span id="stationsCount">0</span> of <?php echo $station_count; ?></div>
            <div id="scrollArea">
                Loading Data...
            </div>
            <div class="title">Legend</div>
            <div id="legend">
                <div class="station"><img src="images/sls.png" />Water Level</div>
                <div class="station"><img src="images/buoy.png" />Waves</div>
                <div class="station"><img src="images/rain.png" />Rain</div>
                <div class="station"><img src="images/wind.png" />Wind</div>
            </div>
            <div>
                <input type="button" value="Reset Query" onclick="window.location='index.php?page=query'"/>
                <input type="button" value="Modify Query" onclick="window.location='index.php?page=query#' + $.param(queryState,true)"/>
            </div>
    </div>
</div>
<?php
require ('inc/google-key.php' );
$jslist[] = 'js/jquery.history.js';
$jslist[] = 'js/map-common.js';
$jslist[] = 'js/results.js';
?>
