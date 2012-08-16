
package pscp.restlet.resource;

import dao.DAOException;
import dao.pscp.Citations;
import dao.pscp.DataSets;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import java.io.File;
import org.restlet.data.Form;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.CitationIngester;
import pscp.restlet.util.Ingester;
import pscp.restlet.util.StationIngester;

/**
 * @todo file storage on success/failure
 * @author iws
 */
public class CitationUploadForm extends IngestResource {

    public CitationUploadForm() {
        setAllowCaching(false);
    }

    @Override
    protected String getServiceTitle() {
        return "Citations Bulk Upload";
    }

    @Override
    protected Ingester buildIngester(Form fields, File uploadedFile, StringBuilder headerMessages) {
        CitationIngester ingester = new CitationIngester();
        ingester.setInputFile(uploadedFile);
        return ingester;
    }

    @Override
    protected void postIngest(Ingester ingester) {
        daoModified(Citations.class);
    }

    @Override
    protected ResourceTemplate buildTemplate() throws DAOException, TemplateException {
        SimpleHash model = new SimpleHash();
        SimpleHash atts = new SimpleHash();
        atts.put("inputFile",TemplateModels.createField("inputFile", "Input File"));
        atts.put("dryRun",TemplateModels.createField("dryRun", "Dry Run"));
        model.put("atts", atts);
        return ResourceTemplate.get("citation-upload-form.html", "form", model);
    }
}
