package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.pscp.ProductNames;
import dao.pscp.Products;
import freemarker.template.TemplateModel;
import java.util.List;
import org.restlet.resource.Representation;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public class ProductForm extends FormResource {

    @Override
    protected TemplateModel getFormModel(Row editing) throws DAOException {
        TemplateModels.FormModel form = new TemplateModels.FormModel();
        List<Pair> formAtts = pairs(formAtts(), editing, false);
        Column<String> productGroup = new Column<String>("productgroup", "Product Group", String.class);
        String groupID = null;
        if (editing != null) {
            ProductNames dao = dao(ProductNames.class);
            RowIterator typeRows = dao.read(editing.value(Products.NAMEID));
            Row type = typeRows.next();
            groupID = type.value(ProductNames.TYPEID).toString();
        }
        formAtts.add(productGroup.pair(groupID));
        form.setAttributes(formAtts);
        form.setHidden(pairs(formHidden(), editing, false));
        form.setAutocompleteURL(Products.STATIONID, urls().getURL(Name.AC_STATION));
        form.setAutocompleteURL(Products.NAMEID, urls().getURL(Name.AC_PRODUCT_NAME));
        form.setParameters(productGroup, new AutoCompleteResource.ProductType().asParamArray(this));
        return form;
    }

    @Override
    protected void initFormResource() {
        setFormTemplate("products-form.html");
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        String id = (String) getRequest().getAttributes().get("id");
        return id == null ? null : dao(Products.class).readProduct(Integer.parseInt(id));
    }

    @Override
    protected String objectName() {
        return "Product";
    }

    @Override
    protected Column[] formAtts() {
        return new Column[]{
                    Products.STATIONID,
                    Products.NAMEID
                };
    }

    @Override
    protected Column[] formHidden() {
        return new Column[]{
                    Products.ID
                };
    }

    @Override
    protected void accept(Representation entity) throws DAOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
