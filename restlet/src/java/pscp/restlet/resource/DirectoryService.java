
package pscp.restlet.resource;

import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.Template;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import pscp.restlet.URLS;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public class DirectoryService extends BaseResource {

    @Override
    protected void initResource() {
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        try {
            Template template = getTemplate("form-template.html");
            SimpleHash model = adminPageModel("Service Directory");
            model.put("contentTemplate", "service-directory.html");
            model.put("links",buildLinksModel());
            return render(template, model);
        } catch (Exception ex) {
            Logger.getLogger(DirectoryService.class.getName()).log(Level.SEVERE, null, ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
        }
    }

    private SimpleSequence buildLinksModel() {
        SimpleSequence links = new SimpleSequence();
        URLS urls = urls();
        URLS.Name[] names = new URLS.Name[] {
            URLS.Name.CONTACT,
            URLS.Name.CONTACT_FORM,
            URLS.Name.PRODUCT,
            URLS.Name.PRODUCT_FORM,
            URLS.Name.STATION,
            URLS.Name.STATION_FORM
        };
        for (URLS.Name namedURL : names) {
            if (namedURL.name().startsWith("AC_")) continue;
            links.add(TemplateModels.createLinkModel(
                    urls.getURL(namedURL),
                    namedURL.displayName()));
        }
        return links;
    }





}
