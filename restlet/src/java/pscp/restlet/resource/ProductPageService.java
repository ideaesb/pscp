
package pscp.restlet.resource;

import dao.DAOException;
import dao.Row;
import dao.pscp.ProductPage;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import org.restlet.data.MediaType;
import org.restlet.resource.Variant;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.util.Hardcoded;

/**
 *
 * @author iws
 */
public class ProductPageService extends TemplateResource {

    @Override
    protected String getServiceTitle() {
        return "Product Page";
    }
    protected void initTemplateResource() {
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    protected ResourceTemplate buildTemplate() throws DAOException, TemplateException {
        SimpleHash model = new SimpleHash();
        Row page = dao(ProductPage.class).getProductPage((String) getRequest().getAttributes().get("id"));
        model.put("stationname", page.string(ProductPage.STATIONNAME));
        model.put("stationclass", page.string(ProductPage.STATIONCLASS));
        model.put("region", page.string(ProductPage.REGION));
        String processName = Hardcoded.getProcessDisplayName(page.string(ProductPage.PROCESS));
        model.put("process", processName);
        String productname = lookup().PRODUCT_TYPES.getValue().get(page.value(ProductPage.NAMEID));
        model.put("name", productname);
        model.put("quality", page.string(ProductPage.QUALITY));
        String creator = page.string(ProductPage.PERSON) + " <" + page.string(ProductPage.EMAIL) + ">";
        model.put("creator", creator);
        ResourceTemplate template = ResourceTemplate.get("product-page.html", "product",model);
        return template;
    }

}
