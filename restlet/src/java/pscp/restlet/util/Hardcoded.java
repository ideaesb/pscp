
package pscp.restlet.util;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iws
 */
public class Hardcoded {
    
    static final Map<String,String> processDisplayNames;
    static final Map<String,String> stationTypeDisplayNames;
    static final Map<String,String> statusDisplayNames;
    static {
        processDisplayNames = new HashMap<String, String>();
        processDisplayNames.put("HS","High Seas");
        processDisplayNames.put("HR","Heavy Rains");
        processDisplayNames.put("SW","Strong Winds");

        stationTypeDisplayNames = new HashMap<String,String>();
        stationTypeDisplayNames.put("SLS", "Sea Level Station");
        stationTypeDisplayNames.put("WB", "Wave Buoy");
        stationTypeDisplayNames.put("Met", "Meteorological Station");
        stationTypeDisplayNames.put("NWS Coop", "National Weather Service Cooperative Program");
        stationTypeDisplayNames.put("Precip Only", "Precipitation Only");
        stationTypeDisplayNames.put("?", "Unknown");

        statusDisplayNames = new HashMap<String, String>();
        statusDisplayNames.put("A", "Active");
        statusDisplayNames.put("I", "Inactive");
        statusDisplayNames.put("U", "Unknown");
    }

    public static String getStatusDisplayName(String code) {
        return statusDisplayNames.get(code);
    }

    public static String getProcessIcon(String processCode) {
        return processCode.toLowerCase() + ".gif";
    }

    public static String getProcessImage(String processCode) {
        String code = processCode.toLowerCase();
        String ext = "sw".equals(code) ? "jpg" : "png";
        return code + "." + ext;
    }

    public static String getProcessDisplayName(String processCode) {
        String display = processDisplayNames.get(processCode);
        // it should never be null, but just in case
        return display == null ? "Unknown process " + processCode : display;
    }
    
    public static String getStationTypeDisplayName(String stationType) {
        String display = stationTypeDisplayNames.get(stationType);
        // it should never be null, but just in case
        return display == null ? "Unknown station type " + stationType : display;
    }
    
}
