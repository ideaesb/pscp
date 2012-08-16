
package pscp.restlet.resource;

import dao.DAOException;
import dao.pscp.DataSets;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import java.io.File;
import org.restlet.data.Form;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.Ingester;
import pscp.restlet.util.StationIngester;

/**
 * @todo file storage on success/failure
 * @author iws
 */
public class StationUploadForm extends IngestResource {

    public StationUploadForm() {
        setAllowCaching(false);
    }

    @Override
    protected String getServiceTitle() {
        return "Station Bulk Upload";
    }

    @Override
    protected Ingester buildIngester(Form fields, File uploadedFile, StringBuilder headerMessages) {
        StationIngester ingester = new StationIngester();
        ingester.setAllowUpdate("on".equals(fields.getFirstValue("allowUpdate", "off")));
        ingester.setAllowOperatorDefinitions("on".equals(fields.getFirstValue("createOperators","off")));
        ingester.setInput(uploadedFile);
        return ingester;
    }

    @Override
    protected void postIngest(Ingester ingester) {
        daoModified(Stations.class);
        daoModified(DataSets.class);
    }

    @Override
    protected ResourceTemplate buildTemplate() throws DAOException, TemplateException {
        SimpleHash model = new SimpleHash();
        SimpleHash atts = new SimpleHash();
        atts.put("inputFile",TemplateModels.createField("inputFile", "Input File"));
        atts.put("allowUpdate",TemplateModels.createField("allowUpdate", "Allow Update"));
        atts.put("createOperators",TemplateModels.createField("createOperators", "Create Operators"));
        atts.put("dryRun",TemplateModels.createField("dryRun", "Dry Run"));
        model.put("atts", atts);
        return ResourceTemplate.get("station-upload-form.html", "form", model);
    }
}
