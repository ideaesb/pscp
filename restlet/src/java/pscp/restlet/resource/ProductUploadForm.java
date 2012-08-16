package pscp.restlet.resource;

import dao.DAOException;
import dao.Row;
import dao.pscp.Contacts;
import dao.pscp.Products;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateException;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.BackgroundExecutor;
import pscp.restlet.util.Ingester;
import pscp.restlet.util.ProductIngester;

/**
 * @author iws
 */
public class ProductUploadForm extends IngestResource {

    @Override
    protected String getServiceTitle() {
        return "Product Bulk Upload";
    }

    @Override
    protected Ingester buildIngester(Form fields, File uploadedFile,StringBuilder headerBuffer) {
        String source = fields.getFirstValue("productSource");
        if (source == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            getResponse().setEntity("The product source must be specified", MediaType.TEXT_HTML);
            return null;
        }
        boolean autoRevision = false;
        boolean allowCreateStation = false;
        Row submitter = currentUser();
        Row contact = submitter;
        if (submitter.value(Contacts.ADMIN)) {
            String contactID = fields.getFirstValue("contact");
            if (contactID != null) {
                contact = lookup().CONTACTS.getValue().get(contactID);
            }
            autoRevision = "on".equals(fields.getFirstValue("autoRevision","off"));
            allowCreateStation = "on".equals(fields.getFirstValue("allowProductCreation","off"));
        }
        ProductIngester ingester = new ProductIngester();
        
        ingester.setScaleProducts(true);
        ingester.setAutoRevision(autoRevision);
        ingester.setAllowCreateProductDefinitions(allowCreateStation);
        ingester.setProductSource(source);
        ingester.setDest(getProductRoot());
        ingester.setInputFile(uploadedFile);
        ingester.setSubmitter(submitter.value(Contacts.CONTACTID));
        ingester.setProductContact(contact.value(Contacts.CONTACTID));
        ingester.setExtractAsynchronously(true);
        return ingester;
    }

    @Override
    protected void postIngest(Ingester ingester) {
        daoModified(Products.class);
        try {
            ProductIngester productIngester = (ProductIngester) ingester;
            BackgroundExecutor.BatchDefinition batch = productIngester.getExtractTasks();
            getJobExecutor().submitJobs(batch);
            getLogger().info("Scheduled " + batch.getNumberOfJobs() + " scaling jobs");
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE,"Error extracting products",ex);
        } catch (DAOException ex) {
            getLogger().log(Level.SEVERE,"Database error",ex);
        }
    }

    @Override
    protected ResourceTemplate buildTemplate() throws DAOException, TemplateException {
        SimpleHash model = new SimpleHash();
        SimpleHash atts = new SimpleHash();
        atts.put("inputFile", TemplateModels.createField("inputFile", "Input File"));
        atts.put("productSource", TemplateModels.createField("productSource", "Product Source"));
        atts.put("dryRun", TemplateModels.createField("dryRun", "Dry Run"));
        atts.put("allowProductCreation", TemplateModels.createField("allowProductCreation", "Create Product Definitions"));
        atts.put("autoRevision", TemplateModels.createField("autoRevision", "Auto Revision"));
        SimpleHash contact = TemplateModels.createField("contact", "Submitter");
        SimpleSequence contactParams = new SimpleSequence();
        Collection<Row> contacts = lookup().CONTACTS.getValue().values();
        for (Row next : contacts) {
            SimpleHash hash = new SimpleHash();
            hash.put("name", next.string(Contacts.PERSON));
            hash.put("value", next.string(Contacts.CONTACTID));
            contactParams.add(hash);
        }
        contact.put("params", contactParams);
        atts.put("contact", contact);
        model.put("atts", atts);
        return ResourceTemplate.get("product-upload-form.html", "form", model);
    }
}
