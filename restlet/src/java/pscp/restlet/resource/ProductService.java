package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Contacts;
import dao.pscp.Products;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import pscp.restlet.URLS;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.RowTransformer;

/**
 *
 * @author iws
 */
public class ProductService extends ServiceResource {

    static {
        declareDAODependency(ProductService.class, Products.class);
    }

    @Override
    protected void initServiceResource() {
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        Products dao = dao(Products.class);
        RowIterator rows = null;
        String id = (String) getRequest().getAttributes().get("id");
        if (id != null) {
            rows = dao.readProduct(Integer.parseInt(id));
        } else {
            rows = dao.read();
        }
        return rows;
    }

    @Override
    protected RowTransformer getRowTransformer(RowIterator rows) {
        final Map<Integer, String> ids = lookup().STATION_IDS.getValue();
        final Map<String, Row> contactRows = lookup().CONTACTS.getValue();
        final Map<Integer, String> productTypes = lookup().PRODUCT_TYPES.getValue();
        return new RowTransformer() {

            public TemplateModel row(Row row) {
                SimpleSequence seq = new SimpleSequence();
                URLS urls = urls();
                Integer stationID = row.value(Products.STATIONID);
                String stationName = ids.get(stationID);
                String stationLink = href(
                        urls.getURL(Name.STATION,"id",stationID.toString())
                        ,stationName);
                seq.add(stationLink);
                seq.add(productTypes.get(row.value(Products.NAMEID)));
                Row contact = contactRows.get(row.string(Products.CONTACTID));
                seq.add(contact.string(Contacts.PERSON));
                if (isUserLoggedIn()) {
                    seq.add(href(urls.getURL(Name.PRODUCT_FORM, "id", row.string(Products.ID)), "Edit"));
                    seq.add(href(urls.getURL(Name.PRODUCT_REVISIONS, "id", row.string(Products.ID)), "Rev"));
                }
                return seq;
            }
        };
    }

    @Override
    protected List<Column> tableColumns(RowIterator rows) {
        ArrayList<Column> cols = new ArrayList<Column>();
        cols.add(Products.STATIONID);
        cols.add(Products.NAMEID);
        cols.add(Products.CONTACTID);
        if (isUserLoggedIn()) {
            cols.add(new Column("edit", "Edit", String.class));
        }
        return cols;
    }

    @Override
    protected String getServiceTitle() {
        return "Products";
    }
}
