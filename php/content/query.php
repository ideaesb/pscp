<?php $page_title = "Product Query"; ?>
<div id="contentArea">
    <div id="centerArea">
        <div class="content">
            <h1>Pacific Storms Climatology Products</h1>
            <div class="helpText">To view a Pacific Storms climatology product:
                <ol class="flushBullets" style="margin-top:4px">
                    <li>Select from among the regions identified in the map shown below.</li>
                    <li>Select a Strong Winds, Heavy Rains, and High Seas process and associated Indicator from among the list on the right.</li>
                    <li>Select a Product Type from among the list on the right.</li>
                    <li>Select the submit query button to initiate the query.</li>
                    <li>From the query results page, select the re-set button to return to this page and submit a new query.</li>
                </ol>
            </div>
            <div class="jswarn">
                <noscript>Your browser does not support JavaScript! Please enable it to use this tool.</noscript>
            </div>
            <div id="map" style="position:relative;width:399px;margin:0 auto;">
                <a id="RNNP" class="maplabel" style="top:40px;left:102px;width:90px;">Northern North Pacific</a>
                <a id="RCNP" class="maplabel" style="top:113px;left:135px;width:90px;">Central North Pacific</a>
                <a id="RWNP" class="maplabel" style="top:113px;left:40px;width:90px;">Western North Pacific</a>
                <a id="RENP" class="maplabel" style="top:113px;left:234px;width:90px;">Eastern North Pacific</a>
                <a id="RCSP" class="maplabel" style="top:223px;left:115px;width:90px;">Central South Pacific</a>
                <a id="RESP" class="maplabel" style="top:223px;left:273px;width:90px;">Eastern South Pacific</a>
                <img height="306" border="0" width="399" src="images/map2.jpg"/>
            </div>
            <p><a href="/docs/about/PSCP DDP 10 guide_wsv.doc">Click here</a> for more information about the data, and the data treatment and analysis protocols.</p>
            <div class="caveat"<b>Note</b>:  Only a very limited number of <i>Strong Winds</i> products are available at this time.</div>
            <br /><br />
        </div>
    </div>
    <div id="rightArea">
        <h5>By Process &amp; Indicator</h5>
        <ul id="inputProcessIndicator">
            <li><span id="PSW" class="group">Strong Winds</span>
                <ul class="choices">
                    <li id="IAll">All Quadrants</li>
                    <li id="IEach">Each Quadrant</li>
                </ul>
            </li>
            <li><span id="PHR" class="group">Heavy Rains</span>
                <ul class="choices">
                    <li id="I1_day">1 day</li>
                    <li id="I5_day">5 day</li>
                    <li id="I30_day">30 day</li>
                </ul>
            </li>
            <li><span id="PHS" class="group">High Seas</span>
                <ul class="choices">
                    <li id="Iheight">Wave height</li>
                    <li id="Ipower">Wave power</li>
                    <li id="Iobserved-mixed">Water level - observed</li>
                    <li id="Iresidual-mixed">Water level - non-tidal residual</li>
                </ul>
            </li>
        </ul>
        <h5>and By Product Type</h5>
        <ul id="inputProductType">
            <li><span class="group">Foundational</span>
                <ul class="choices">
                    <li id="T1">Time Series</li>
                    <li id="T2">Cumulative Distribution Function</li>
                </ul>
            </li>
            <li><span class="group">Inter-Annual</span>
                <ul class="choices">
                    <li id="T3">Frequency Counts</li>
                    <li id="T7">Exceedance Probabilities</li>
                    <li id="T9">Regression - Full POR</li>
                    <li id="T10">Regression - Contrasting POR</li>
                    <li id="T11">Extreme Value-based Trends</li>
                </ul>
            </li>
            <li><span class="group">Annual</span>
                <ul class="choices">
                    <li id="T16">Daily Time Series</li>
                    <li id="T17">Full POR</li>
                    <li id="T18">Contrasting POR</li>
                    <li id="T19">Monthly Frequency Counts</li>
                </ul>
            </li>
            <li><span class="group">Climate Indices</span>
                <ul class="choices">
                    <li id="T21">Paired Time Series</li>
                </ul>
            </li>
        </ul>
        <div>
            <input disabled="true" type="button" value="Submit Query" id="submitQuery"/>
        </div>
    </div>
</div>
<?php
$jslist[] = 'js/map-common.js';
$jslist[] = 'js/query.js';
$csslist[] = 'css/query.css';
// preload google
require ('inc/google-key.php' );
?>