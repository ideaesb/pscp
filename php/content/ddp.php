<?php require ('inc/db.php' ); ?>
<style type="text/css">
    @import url(css/map.css);
</style>
<table  border="0" cellspacing="0" cellpadding="0" width="100%">
    <tr>
        <td width="20%" valign="top" bgcolor="#E6F3FF">
            <table id="controls" align="center" width="100%" cellspacing="0">
                <tr>
                    <td>
                        <div class="title" title="Select a product to view stations that have products of the selected type.">Product Group/Type</div>
                        <select name="type" id="selectType" onchange="javascript:document.getElementById('filterzzz').style.display='block';">
                            <option value="-1" style="color:red;background-color:yellow;">CHOOSE ONE FIRST...</option>
                            <option disabled class="selectHeading" >Foundational</option>
                            <option value="1">&nbsp;&nbsp;&nbsp;Time Series</option>
                            <option value="2">&nbsp;&nbsp;&nbsp;Cumulative Distribution Function</option>
                            <option disabled class="selectHeading">Inter-Annual</option>
                            <option value="3">&nbsp;&nbsp;&nbsp;Frequency Counts</option>
                            <option value="7">&nbsp;&nbsp;&nbsp;Exceedance Probabilities</option>
                            <option value="9">&nbsp;&nbsp;&nbsp;Regression - Full POR</option>
                            <option value="10">&nbsp;&nbsp;&nbsp;Regression - Contrasting POR</option>
                            <option value="11">&nbsp;&nbsp;&nbsp;Extreme Value-based</option>
                            <option disabled class="selectHeading">Annual</option>
                            <option value="16">&nbsp;&nbsp;&nbsp;Daily Time Series</option>
                            <option value="17">&nbsp;&nbsp;&nbsp;Full POR</option>
                            <option value="18">&nbsp;&nbsp;&nbsp;Contrasting POR</option>
                            <option value="19">&nbsp;&nbsp;&nbsp;Monthly Frequency Counts</option>
                            <option disabled class="selectHeading">Climate Indices</option>
                            <option value="21">&nbsp;&nbsp;&nbsp;Paired Time Series</option>
                        </select>

                        
                    </td>
                </tr>
             </table>
             <table id="filterzzz" style="display:none;" align="center" width="100%" cellspacing="0">     
                <tr>
                <td title="Refine the station query by data quality">
                        <div class="title">Filter By Data Quality:</div>
                        <div class="control">All Levels : <input type="radio" value="" name="quality" checked></div>
                        <div class="control">Level 1 : <input type="radio" value="1" name="quality"></div>
                        <div class="control">Level 2 : <input type="radio" value="2" name="quality"></div>
                        <div class="control">Level 3 : <input type="radio" value="3" name="quality"></div>
                </td>
                </tr>
                <tr>
                    <td title="Refine the station query by product process">
                        <div class="title">Filter By Process:</div>
                        <div class="control">All Processes :<input type="radio" value="" name="process" checked="yes"></div>
                        <div class="control">Heavy Rain :<input type="radio" value="HR" name="process"></div>
                        <div class="control">High Seas :<input type="radio" value="HS" name="process"></div>
                        <div class="control">Strong Winds : <input type="radio" value="SW" name="process"></div>
                    </td>
                </tr>
                <tr>
                    <td title="Refine the station query by region">
                        <div class="title">Filter By Region:</div>
                        <div class="control">All Regions<input type="radio" value="" name="region" checked="yes"></div>
                        <div class="control">Northern North Pacific<input type="radio" value="NNP" name="region"></div>
                        <div class="control">Eastern North Pacific<input type="radio" value="ENP" name="region"></div>
                        <div class="control">Central North Pacific<input type="radio" value="CNP" name="region"></div>
                        <div class="control">Western North Pacific<input type="radio" value="WNP" name="region"></div>
                        <div class="control">Central South Pacific<input type="radio" value="CSP" name="region"></div>
                        <div class="control">Eastern South Pacific<input type="radio" value="ESP" name="region"></div>
                    </td>
                </tr>

            </table>
        </td>

        <td class="bodyText" width="60%" valign="top">
            <div class="title" id="topcontrols">
                <a title="Display the help message" id="help">[ Help ]</a>
                <a title="Toggle between the product or map viewing mode" id="showToggle">[ Show Products ]</a>
                <span id="maptitle">Derived Data Products Map</span>
            </div>
            <div id="productoverlay" class="mapoverlay" >
                <div id="products">
                    <h4>No Station Selected</h4>
                </div>
            </div>
            <div id="helpoverlay" class="mapoverlay" >
                <div class="helpText">
                	Select a Product Type from among the list on the left to view an interactive map
                	showing all locations where a particular type of Pacific Storms climatology product
                	is available.<br /><br />

                	With the interactive map before you, select an individual station icon (point and click)
                	to display a box that contains basic information about the station, data, and products.
                	Selecting <i>View Products</i> will direct you to a page that links to all products of the
                	identified type available for that station.<br /><br />

                	Learn more about each product type below (opens in its own window).

                        
                            <div>Foundational
                                <ul class="flushBullets" style="margin-top: 0px">
                                    <li><a target="about" href="index.php?page=about-products#1.1">Time Series</a></li>
                                    <li><a target="about" href="index.php?page=about-products#2.1">Cumulative Distribution Function</a></li>
                                </ul>
                            </div>
                            <div>Inter-Annual
                                <ul class="flushBullets" style="margin-top: 0px">
                                    <li><a target="about" href="index.php?page=about-products#3.1.1">Frequency Counts</a></li>
                                    <li><a target="about" href="index.php?page=about-products#4.1">Exceedance Probabilities</a></li>
                                    <li><a target="about" href="index.php?page=about-products#5.1.1">Regression - Full POR</a></li>
                                    <li><a target="about" href="index.php?page=about-products#5.1.2">Regression - Contrasting POR</a></li>
                                    <li><a target="about" href="index.php?page=about-products#5.2.1">Extreme Value-based Trends</a></li>
                                </ul>
                            </div>
                            <div>Annual
                                <ul class="flushBullets" style="margin-top: 0px">
                                <li><a target="about" href="index.php?page=about-products#6.1">Daily Time Series</a></li>
                                <li><a target="about" href="index.php?page=about-products#7.1">Full POR</a></li>
                                <li><a target="about" href="index.php?page=about-products#7.2">Contrasting POR</a></li>
                                <li><a target="about" href="index.php?page=about-products#8.1">Monthly Frequency Counts</a></li>
                                </ul>
                            </div>
                            <div>Climate Indices
                                <ul class="flushBullets" style="margin-top: 0px">
                                <li><a target="about" href="index.php?page=about-products#9.1">Paired Time Series</a></li>
                                </ul>
                            </div>
                        </ul>
                        <p>    
                            To further refine your search select by data quality, 
                            process, and region from the lists on the left.
                        </p>
                        <div style="text-align: center; ">
                            <img src="images/gfdl.jpg" alt="Image courtesy of NOAA’s Geophysical Fluid Dynamics Laboratory." height="150" width="300"></img>
                            <div class="caption">Image courtesy of NOAA’s Geophysical Fluid Dynamics Laboratory</div>
                        </div>
                </div>
            </div>
            <div id="map" style="height: 600px;">
                <span class="hlink">Select a process, region, or product group.</span>
            </div>

        </td>
        <td width="20%" valign="top" title="Click on a station to select the station on the map or view the products in product mode.">
            <div class="title">Stations - <span id="stationsCount">0</span> of <?php echo $station_count; ?></div>
            <div id="scrollArea">
                <span class="hlink">Select a product group/type.  Then select a quality level, process, or region to narrow your search.</span>
            </div>
            <div class="title">Legend</div>
            <div id="legend">
                <div class="station"><img src="images/sls.png" />Water Level</div>
                <div class="station"><img src="images/buoy.png" />Waves</div>
                <div class="station"><img src="images/rain.png" />Rain</div>
                <div class="station"><img src="images/wind.png" />Wind</div>
            </div>
        </td>
    </tr>
</table>
<div id="imageViewer" title="Click to close.">
    <h4>Click to Close</h4>
    <img src=""/>
</div>
<?php
require ('inc/google-key.php' );
$jslist[] = 'js/jquery.history.js';
$jslist[] = 'js/map-common.js';
$jslist[] = 'js/map.js';
?>
