package pscp.restlet.resource;

import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.Table;
import dao.Tables;
import dao.pscp.ProductLinks;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.restlet.data.Form;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.RestletDAOUtils;

/**
 *
 * @author iws
 */
public class ProductLinkService extends ServiceResource {

    static {
        declareDAODependency(ProductLinkService.class, ProductLinks.class, Stations.class);
    }

    private int stationID;
    private Set<Integer> filterProductTypes;
    private Table products;
    private int version = 1;
    private String[] indicator;
    private String process;
    private boolean showThumbs;

    @Override
    protected TemplateModel createTemplateModel(RowIterator stations) throws DAOException {
        SimpleSequence groups = new SimpleSequence();
        Map<Integer,String> typeNames = lookup().PRODUCT_TYPES.getValue();
        Integer currentGroup = null;
        String productLocationBase = urls().getURL(Name.PRODUCT_LOCATION);
        SimpleSequence groupLinks = null;
        Map<Integer,String> groupNames = lookup().PRODUCT_GROUP_NAMES.getValue();
        Map<Integer,String> groupIDs = lookup().PRODUCT_GROUP_IDS.getValue();
        //String mediaBase = urls().getURL(Name.STATIC_MEDIA);
        int productCount = 0;
        for (Row row: products) {
            Integer groupID = row.value(ProductLinks.TYPEID);
            if (filterProductTypes != null && !filterProductTypes.contains(groupID)) {
                continue;
            }
            String typeName = typeNames.get(row.value(ProductLinks.NAMEID));
            if (process != null && typeName.indexOf(process) < 0) {
                continue;
            }
            productCount++;
            if (currentGroup == null || !currentGroup.equals(groupID)) {
                currentGroup = groupID;
                SimpleHash group = new SimpleHash();
                String groupName = groupNames.get(groupID);
                group.put("name", groupName);
                // @hack
                String groupString = groupIDs.get(groupID);
                group.put("about", "index.php?page=about-products#" + groupString);
                group.put("links", groupLinks = new SimpleSequence());
                groups.add(group);
            }
            String url = row.string(ProductLinks.LOCATION);
            String thumb = null;
            if (!url.startsWith("http")) {
                url = productLocationBase + url;
                int idx = url.lastIndexOf('.');
                StringBuilder thumbString = new StringBuilder();
                thumbString.append(url,0,idx);
                thumbString.append("_scaled");
                thumbString.append(".gif");
                thumb = thumbString.toString();
            }
            SimpleHash link = TemplateModels.createLinkModel(
                    url, typeName);
            if (indicator.length > 0) {
                boolean match = false;
                for (int i = 0; i < indicator.length; i++) {
                    if (indicator[i] == null) continue;
                    if (typeName.indexOf(indicator[i]) > 0) {
                        match = true;
                        break;
                    }
                }
                if (!match) {
                    continue;
                }
            }
            if (thumb != null) {
                link.put("thumb", thumb);
            }
            link.put("pageurl","/index.php?page=product&id=" + row.string(ProductLinks.LOCATION) + "&type=" + groupIDs.get(groupID));
            groupLinks.add(link);
        }
        SimpleHash model = new SimpleHash();
        if (stationID > 0) {
            Row station = stations.next();
            model.put("stationname",station.string(Stations.STATIONNAME));
            model.put("stationpage",urls().getURL(Name.STATION_PAGE,"id",station.string(Stations.STATIONID)));
        }
        model.put("showthumbs",showThumbs);
        if (productCount == 0) {
            model.put("message",productCount + " products found");
        }
        model.put("linkgroups", groups);
        return model;
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        Stations dao = dao(Stations.class);
        RowIterator stations = null;
        if (stationID > 0) {
            stations = dao.readStation(stationID);
            products = Tables.table(dao(ProductLinks.class).readProductsAtStation(stationID));
        } else {
            stations = dao.readStations(null, null);
            List<Pair> searchParams = RestletDAOUtils.parseForm(getRequest().getOriginalRef().getQueryAsForm(), ProductLinks.NAMEID);
            products = Tables.table(dao(ProductLinks.class).search(searchParams));
        }
        return stations;
    }

    @Override
    protected String getServiceTemplate() {
        //@todo Hack
        return version == 1 ? "product-links.html" : "product-links2.html";
    }

    @Override
    protected void initServiceResource() {
        String id = (String) getRequest().getAttributes().get("id");
        if (id != null) {
            stationID = Integer.parseInt(id);
        }
        Form form = getRequest().getOriginalRef().getQueryAsForm();
        String[] typeids = form.getValuesArray("typeid");
        if (typeids.length > 0) {
            filterProductTypes = new HashSet<Integer>();
            for (int i = 0; i < typeids.length; i++) {
                filterProductTypes.add( new Integer(typeids[i]) );
            }
        }
        //@todo Hack
        if ("2".equals(form.getFirstValue("version","1"))) {
            version = 2;
        }
        indicator = form.getValuesArray("indicator");
        process = form.getFirstValue("process");
        showThumbs = Boolean.parseBoolean(form.getFirstValue("thumbs","false"));
    }

    @Override
    protected String getServiceTitle() {
        return "Product Links";
    }

}
