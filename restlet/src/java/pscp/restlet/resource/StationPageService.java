
package pscp.restlet.resource;

import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.DataSets;
import dao.pscp.ProductLinks;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Status;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.Hardcoded;

/**
 *
 * @author iws
 */
public class StationPageService extends TemplateResource {
    static {
        declareDAODependency(StationPageService.class, Stations.class,ProductLinks.class);
    }

    private Row station;
    private int stationID = -1;
    private boolean isTestMode = false;

    public StationPageService() {
        super();
    }

    @Override
    protected String getServiceTitle() {
        return "Station Page";
    }

    @Override
    protected void initTemplateResource() {
        try {
            stationID = Integer.parseInt((String)getRequest().getAttributes().get("id"));
        } catch (NumberFormatException nfe) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST,"Invalid station id");
        }
        if (stationID >= 0) {
            try {
                station = dao(Stations.class).readStation(stationID).next();
            } catch (DAOException ex) {
                getLogger().log(Level.SEVERE,"Error getting station " + stationID,ex);
            }
            if (station == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND,"No station with id " + stationID);
            }
        }
    }

    private SimpleHash stationHash(Row station,Row dataset) {
        SimpleHash stationHash = new SimpleHash();
        // various fields
        stationHash.put("image", Hardcoded.getProcessImage(station.string(Stations.PROCESS)));
        stationHash.put("icon", Hardcoded.getProcessIcon(station.string(Stations.PROCESS)));
        // station fields
        stationHash.put("process", Hardcoded.getProcessDisplayName(station.string(Stations.PROCESS)));
        stationHash.put("name", station.string(Stations.STATIONNAME));
        stationHash.put("nation", station.string(Stations.NATION));
        stationHash.put("instrument", station.string(Stations.STATIONCLASS));
        stationHash.put("operator", lookup().OPERATORS.getValue().get(station.value(Stations.ONUM)));
        stationHash.put("latitude", station.string(Stations.LATITUDE));
        stationHash.put("longitude", station.string(Stations.LONGITUDE));
        stationHash.put("status", Hardcoded.getStatusDisplayName(station.string(Stations.STATUS)));
        stationHash.put("metadata", href( station.string(Stations.METADATA), null ));
        // dataset fields
        stationHash.put("source", dataset.string(DataSets.SOURCE));
        stationHash.put("sourceid", dataset.string(DataSets.SOURCEID));
        stationHash.put("quality", dataset.string(DataSets.QUALITY));
        stationHash.put("datametadata", href( dataset.string(DataSets.METADATAURL), null));
        return stationHash;
    }
    
    @Override
    protected ResourceTemplate buildTemplate() throws DAOException {
        ResourceTemplate stationPageTemplate = null;
        try {
            if (isTestMode) {
                String testStr = null;
                String str2 = testStr.toLowerCase(); // intentionally cause a null pointer exception
            }
            
            Row dataset = dao(DataSets.class).read(station.value(Stations.DATASETID));
            SimpleHash stationHash = stationHash(station,dataset);
            RowIterator products = dao(ProductLinks.class).readProductsAtStation(stationID);

            stationPageTemplate = ResourceTemplate.get("station-page.html","station", stationHash);

            SimpleSequence groups = new SimpleSequence();
            Map<Integer,String> typeNames = lookup().PRODUCT_TYPES.getValue();
            Integer currentGroup = null;
            String productLocationBase = urls().getURL(Name.PRODUCT_LOCATION);
            SimpleSequence groupLinks = null;
            Map<Integer,String> groupNames = lookup().PRODUCT_GROUP_NAMES.getValue();
            Map<Integer,String> groupTypes = lookup().PRODUCT_GROUP_IDS.getValue();
            while (products.hasNext()) {
                Row row = products.next();
                Integer groupID = row.value(ProductLinks.TYPEID);
                if (currentGroup == null || !currentGroup.equals(groupID)) {
                    currentGroup = groupID;
                    SimpleHash group = new SimpleHash();
                    String groupName = groupNames.get(groupID);
                    group.put("name", groupName);
                    // @hacky
                    group.put("about", "/index.php?page=about-products#" + groupTypes.get(groupID));
                    group.put("links", groupLinks = new SimpleSequence());
                    groups.add(group);
                }
                final String productLocation = row.string(ProductLinks.LOCATION);
                String thumb = null;
                if (!productLocation.startsWith("http")) {
                    thumb = productLocationBase + productLocation;
                    int idx = thumb.lastIndexOf('.');
                    StringBuilder thumbString = new StringBuilder();
                    thumbString.append(thumb,0,idx);
                    thumbString.append("_scaled.gif");
                    thumb = thumbString.toString();
                }
                String productURL = "/index.php?page=product&type=" + groupTypes.get(groupID) + "&id=" + productLocation;
                SimpleHash link = TemplateModels.createLinkModel(
                        productURL, typeNames.get(row.value(ProductLinks.NAMEID)));
                if (thumb != null) {
                    link.put("thumb", thumb);
                }
                link.put("date",row.string(ProductLinks.CHANGED));
                groupLinks.add(link);
            } // while (products.hasNext())
            stationPageTemplate.addModel("products", groups);
        }
        catch (Exception e) {
            String name = e.getMessage();
            if (name == null) {
                name = "station page error.";
            }
            SimpleHash errorHash = new SimpleHash();
            errorHash.put("type", e.getClass().toString());
            errorHash.put("message", "Error attempting to fetch the station page for "+stationID+". The system is unable to fulfil your request at this time. Please try again later. ");
            errorHash.put("reason", e.toString());
            errorHash.put("continue", "/index.php");
            stationPageTemplate = ResourceTemplate.get("service-error.html", "error", errorHash);
            // stationPageTemplate.addModel("products", new SimpleSequence());
        }
        return stationPageTemplate;
    }

}
