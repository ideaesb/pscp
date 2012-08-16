<script>
    
        var _hover = "";
    
        function toggleShutter() {
            var isOpen = (document.getElementById("shutterArea").style.display=="block");
            document.getElementById("shutterArea").style.display = (isOpen ? "none":"block");
            document.getElementById("shutterControl").src = (isOpen ? "images/up"+_hover+".gif" : "images/down"+_hover+".gif");
            document.getElementById("shutterControl").title = (isOpen ? "Click to show related links" : "Click to hide related links.");
        }
        
        function hoverImage() {
            var isOpen = (document.getElementById("shutterArea").style.display=="block");
            document.getElementById("shutterControl").src = (isOpen ? "images/down"+_hover+".gif" : "images/up"+_hover+".gif");
        }
        
        function hover() {
            _hover = "_hover"; 
            hoverImage();
        }
        
        function unHover() {
            _hover = "";
            hoverImage();
        }
</script>
<div id="leftNavUserExtras">
    <table id="leftNavExtrasControl">
        <tr>
            <td class="leftNavExtrasHeading">Related Links</td>
            <td style="text-align: right"><span onclick="toggleShutter()" onmouseover="hover()" onmouseout="unHover()"><img id="shutterControl" class="clickable" src="images/up.gif" alt="shutter" title="Click to show related links." ></img></span></td>
        </tr>
        <tr>
            <td id="shutterArea" colspan="2" style="display:none">
                <ul style="padding-top: 0px; margin-top: 0px">
                    <li id="leftNavPacificIslands"><a href="http://apdrc.soest.hawaii.edu/index.php" target="_blank" title="The APDRC provides an extensive range of climate-related data and products in multiple formats. ">Asia Pacific Data Research Center of the IPRC</a></li>
                    <li id="leftNavPacificIslands"><a href="http://csc.noaa.gov/hurricanes/#" target="_blank" title="Learn about historical tropical cyclones. The interactive mapping application presents various Forecast Centers' historical tropical cyclone data and graphically display storms affecting diverse areas since the mid-1800s. U.S. coastal county population vs. hurricane strikes since 1900 is provided.">CSC Historical Hurricane Tracks</a></li>
                    <li id="leftNavPacificIslands"><a href="http://gosic.org/" target="_blank" title="The GOSIC Portal provides convenient, central, one-stop access to data and information identified by the Global Climate Observing System (GCOS), the Global Ocean Observing System (GOOS) and the Global Terrestrial Observing System (GTOS) and their partner programs, such as the Global Atmosphere Watch (GAW) and regional observing systems, such as the GOOS Regional Alliances (GRA).">Global Observing Systems Information Center (GOSIC)</a></li>
                    <li id="leftNavPacificIslands"><a href="http://podaac.jpl.nasa.gov/index.html" target="_blank" title="The data center responsible for archiving and distributing data relevant to the physical state of the ocean. Most of the data products available at PO.DAAC were obtained from Earth observing satellites and are intended for scientific research. This site provides access to an extensive catalog of mostly satellite-derived physical oceanographic data and products (e.g. SST, winds, circulation, and salinity).">NASA Physical Oceanography Distributed Active Archive Center (PO.DAAC)</a></li>
                    <li id="leftNavPacificIslands"><a href="http://www.ncdc.noaa.gov/oa/ibtracs/" target="_blank" title="Tropical cyclone best track data aids our understanding of the distribution, frequency, and intensity of tropical cyclones worldwide. The intent of the project is to overcome data availability issues, and freely disseminate this new global dataset. ">NCDC IBTrACS Tropical Cyclone Data</a></li>
                    <li id="leftNavDataDiscovery"><a href="http://gis.ncdc.noaa.gov/geoportal/catalog/main/home.page" target="_blank" title="Geographic Information System (GIS) services at NOAA's National Climatic Data Center (NCDC) provide users with simple access to data archives while integrating new and informative climate products. Systems at NCDC provide a variety of climatic data in GIS formats and/or map viewers. The Online GIS Map Services provide data discovery options which flow into detailed product selection maps that may be queried using standard region finder tools or gazetteer functions. ">NESDIS NCDC Data Discovery</a></li>
                    <li id="leftNavPacificIslands"><a href="http://oceanwatch.pifsc.noaa.gov/index.html" target="_blank" title="Oceanwatch acquires and processes satellite information and creates a variety of satellite data products for the Pacific Ocean region. They serve as an updated source of daily regional satellite oceanographic observations. ">NOAA OceanWatch - Central Pacific</a></li>
                    <li id="leftNavSeaLevels"><a href="http://tidesandcurrents.noaa.gov/sltrends/sltrends.shtml" target="_blank" title="The Center has been measuring sea level for over 150 years, with tide stations operating on all U.S. coasts through the National Water Level Observation Network (NWLON). Changes in Mean Sea Level (MSL), either a sea level rise or sea level fall, have been computed at 128 long-term water level stations using a minimum span of 30 years of observations at each location. The effect of high frequency phenomena, such as waves and tides, have been removed to compute an accurate linear sea level trend. ">NOS COOPS Sea Levels Online</a></li>
                    <li id="leftNavCimatePredictionCenter"><a href="http://www.cpc.ncep.noaa.gov/" target="_blank" title="The Climate Prediction Center's (CPC) products are operational predictions of climate variability, real-time monitoring of climate and the required databases, and assessments of the origins of major climate anomalies. The products cover time scales from a week to seasons, extending into the future as far as technically feasible, and cover the land, the ocean, and the atmosphere, extending into the stratosphere.">NWS Climate Prediction Center</a></li>
                    <li id="leftNavPacificIslands"><a href="http://www.ndbc.noaa.gov/" target="_blank" title="The NDBC manages the development, operations, and maintenance of the national data buoy network. It serves as the NOAA focal point for data buoy and associated meteorological and environmental monitoring technology. It provides high quality meteorological/environmental data in real time from automated observing systems that include buoys and a Coastal-Marine Automated Network (C-MAN) in the open ocean and coastal zone surrounding the United States.">NWS National Data Buoy Center (NDBC)</a></li>
                    <li id="leftNavPacificIslands"><a href="http://www.bom.gov.au/pacificsealevel/tides.shtml" target="_blank" title="Significant oceanographic and meteorological data and related information are readily available and easily accessible to the project partners, the international scientific community, and the public. This page provides access to project generated tidal and meteorological data and information, both current and historical for the period of the Project so far.">South Pacific Sea Level and Climate Monitoring Project (SPSLCMP)</a></li>
                    <li id="leftNavPacificIslands"><a href="http://uhslc.soest.hawaii.edu/" target="_blank" title="The University of Hawaii Sea Level Center (UHSLC) website contains in-situ tide gauge data from around the world in support of climate research.">University of Hawaii Sea Level Center Historical Sea Level data</a></li>
                    <li id="leftNavHistoricWaveData"><a href="http://cdip.ucsd.edu/?&nav=historic&sub=data" target="_blank" title="Access to all Coastal Data Information Program’s (CDIP) data, from the first observations made in 1975 to values just five minutes old, recorded by sensors in the water at this very moment.">USACoE/Scripps CDIP Historic Wave Data</a></li>
                    <li id="leftNavPacificIslands"><a href="http://hi.water.usgs.gov/" target="_blank" title="Water-resource information and products for the State of Hawaii, the U.S. Territories of Guam and American Samoa, the U.S. Commonwealth of the Northern Mariana Islands, the Republic of Palau, the Republic of the Marshall Islands, and the Federated States of Micronesia.">USGS Pacific Islands Water Resource Center</a></li>
                    <li id="leftNavWestern"><a href="http://www.wrcc.dri.edu/" target="_blank" title="Daily climate observations (6,781 stations; 2,608 now active); Summarized monthly climate data (5,240 stations); Hourly precipitation data (1,937 stations); Twice-daily upper air soundings (@ 50 stations); Surface airways hourly observations (over 1,800 stations nationwide); Remote Automatic Weather Station; Historic lightning data thru 1996; Access to Natural Resources Conservation Service SNOTEL and other western databases; 3,000 climate stations for western states; 200,000+ web pages to choose from.">Western Regional Climate Center</a></li>
                </ul>
            </td>
        </tr>
    </table>
</div>
<div id="leftNavExtras">
    <a href="http://www.ncdc.noaa.gov/oa/ncdc.html" target="_blank">
        <img width="128" src="images/partners/ncdc.gif" alt="National Climatic Data Center (NCDC)"/>
    </a>
    <a href="http://uhslc.soest.hawaii.edu/" target="_blank">
        <img width="128" src="images/partners/uhslc.png" alt="University of Hawaii Sea Level Center"/>
    </a>
    <a href="http://www.iarc.uaf.edu/index.php" target="_blank">
        <img width="71" src="images/partners/iarc.png" alt="International Arctic Research Center"/>
    </a>
    <a href="http://www.geo.oregonstate.edu/" target="_blank">
        <img width="128" src="images/partners/osu.png" alt="Oregon State University"/>
    </a>
    <a href="http://www.weriguam.org/" target="_blank">
        <img width="48" src="images/partners/guam.png" alt="University of Guam"/>
    </a>
    <a href="http://www.eastwestcenter.org/home/" target="_blank">
        <img width="150" src="images/partners/EWC_sig50_color.gif" alt="East West Center"/>
    </a>
</div>
