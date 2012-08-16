package pscp.restlet.resource;

import dao.RowIterator;
import pscp.restlet.template.TemplateModels;
import dao.DAOException;
import dao.Row;
import dao.pscp.ProductTypes;
import dao.pscp.ProductNames;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.Map;
import pscp.restlet.URLS.Name;

/**
 * @todo (LOW) split this out into 2?
 * @author iws
 */
public class ProductTypeService extends ServiceResource {

    static {
        declareDAODependency(ProductTypeService.class, ProductTypes.class, ProductNames.class);
    }

    private String id;

    @Override
    protected String getServiceTemplate() {
        return id == null ? "product-type-list.ftl" : "product-name-list.ftl";
    }

    @Override
    protected TemplateModel createTemplateModel(RowIterator rows) throws DAOException {
        TemplateModel model;
        if (id == null) {
            model = TemplateModels.createRowIteratorModel("types", rows);
        } else {
            model = buildProductNamesModel(rows);
        }
        return model;
    }

    private TemplateModel buildProductNamesModel(RowIterator rows) throws DAOException {
        Map<Integer, String> typeNames = lookup().PRODUCT_TYPES.getValue();
        SimpleSequence seq = new SimpleSequence();
        String url = urls().getURL(Name.PRODUCT_LINK,"id","all");
        url += "?nameid=";
        while (rows.hasNext()) {
            Row r = rows.next();
            SimpleHash h = new SimpleHash();
            Integer typeID = r.value(ProductNames.NAMEID);
            h.put("link", url + typeID);
            h.put("name", typeNames.get(typeID));
            h.put("cnt",r.value(ProductNames.CNT));
            seq.add(h);
        }
        SimpleHash hash = new SimpleHash();
        hash.put("names", seq);
        return hash;
    }

    @Override
    protected void initServiceResource() {
        id = (String) getRequest().getAttributes().get("id");
    }

    @Override
    protected String getServiceTitle() {
        return id == null ? "Product Types" : "Product Names";
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        RowIterator rows;
        if (id == null) {
            rows = dao(ProductTypes.class).readGroups();
        } else {
            rows = dao(ProductNames.class).readByGroup(Integer.parseInt(id));
        }
        return rows;
    }
}
