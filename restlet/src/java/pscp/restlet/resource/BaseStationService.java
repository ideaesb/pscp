
package pscp.restlet.resource;

import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Stations;
import java.util.Arrays;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import pscp.restlet.URLS;

/**
 *
 * @author iws
 */
public abstract class BaseStationService extends ServiceResource {

    private String process;
    private String region;
    private Integer quality;
    private String[] groupid;
    private String[] indicator;
    private URLS urls;

    @Override
    protected final void initServiceResource() {
        Form query = getRequest().getOriginalRef().getQueryAsForm();
        process = query.getFirstValue("process");
        region = query.getFirstValue("region");
        try {
            quality = new Integer(query.getFirstValue("quality"));
        }
        catch (NumberFormatException nfe) {
            // nada ok.
            //System.out.println("BaseStationService.initServiceResource error attempting to extract quality argument from query. " + nfe.toString());
        }
        groupid = query.getValuesArray("typeid");
        if (query.getNames().contains("indicator")) {
            indicator = query.getValuesArray("indicator");
            // @HACK #12345 - see below in resolveTable, too
            // need to allow "empty" indicator array to support switch in query logic
            if (indicator.length == 1 && indicator[0] == null) {
                indicator = new String[0];
            }
        }
        configureVariants(MediaType.TEXT_HTML);
        urls = urls();
    }

    protected final String getInfoURL(Row row) {
        return urls.getURL(URLS.Name.STATION, "id", row.string(Stations.STATIONID));
    }

    protected final String getProductsURL(Row row) {
        return urls.getURL(URLS.Name.PRODUCT_LINK, "id", row.string(
                        Stations.STATIONID));
    }

    protected final String getPageURL(Row row) {
        return urls.getURL(URLS.Name.STATION_PAGE, "id", row.string(
                        Stations.STATIONID));
    }

    @Override
    protected final RowIterator resolveTable() throws DAOException {
        return resolveStationsTable(process, region, quality, groupid, indicator);
    }

    protected RowIterator resolveStationsTable(String process, String region, Integer quality, String[] groupid, String[] indicator) throws DAOException {
        RowIterator rows;
        if (indicator != null) {
            // @HACK 12345 - this should be clearer.
            int[] productTypes = new int[groupid.length];
            for (int i = 0; i < productTypes.length; i++) {
                productTypes[i] = Integer.parseInt(groupid[i]);
            }
            rows = dao(Stations.class).readStationLocations(process, region, quality, productTypes, indicator);
        } else if (groupid == null || groupid.length == 0) {
            rows = dao(Stations.class).readStationLocations(process, region, quality);
        } else {
            rows = dao(Stations.class).readStationLocationsByGroup(Integer.parseInt(groupid[0]));
        }
        return rows;
    }

}
