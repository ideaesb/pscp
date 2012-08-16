package pscp.restlet.resource;

import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Citations;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public class CitationFileForm extends TemplateResource {

    private Row citation;
    private int id;
    private File citationDirectory;

    public CitationFileForm() {
        setAllowCaching(false);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    protected void initTemplateResource() {
        citationDirectory = getCitationDirectory();
        String idspec = (String) getRequest().getAttributes().get("id");
        if (idspec == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "No id");
            return;
        }
        try {
            id = Integer.parseInt(idspec);
        } catch (NumberFormatException nfe) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid id");
            return;
        }
        try {
            citation = dao(Citations.class).read(id);
            if (citation == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, "No citation for");
                return;
            }
        } catch (DAOException ex) {
            getLogger().log(Level.SEVERE, "error", ex);
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
        }
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        RestletFileUpload upload = new RestletFileUpload(new DiskFileItemFactory());
        List<FileItem> files = null;
        Form fields = new Form();
        FileItem uploadedFile = null;
        try {
            files = upload.parseRepresentation(entity);
            for (FileItem i : files) {
                if (i.isFormField()) {
                    fields.add(i.getFieldName(), i.getString());
                } else {
                    uploadedFile = i;
                }
            }
        } catch (FileUploadException ex) {
            getLogger().log(Level.SEVERE, null, ex);
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error during upload");
        }
        if (id == 0) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "no id");
            return;
        }
        String msg;
        if (uploadedFile == null) {
            String existing = fields.getFirstValue("existingFile");
            if (existing == null) {
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST, "no file");
                return;
            }
            connectCitation(existing);
            msg = "Set linked file to : " + existing;
            getResponse().setStatus(Status.SUCCESS_ACCEPTED);
        } else {
            connectCitation(uploadedFile.getName());
            try {
                uploadedFile.write(new File(citationDirectory, uploadedFile.getName()));
                msg = "Accepted uploaded file : " + uploadedFile.getName();
                getResponse().setStatus(Status.SUCCESS_ACCEPTED);
            } catch (Exception ex) {
                throw new ResourceException(ex);
            }
        }
        getResponse().setEntity(new StringRepresentation(msg));
    }

    private void connectCitation(String filename) throws ResourceException {
        try {
            dao(Citations.class).setFile(id, filename);
            daoModified(Citations.class);
        } catch (Exception ex) {
            throw new ResourceException(ex);
        }
    }

    private File getCitationDirectory() {
        File citationsDir = new File(getProductRoot().getParentFile(), "library");
        if (!citationsDir.exists()) {
            citationsDir.mkdir();
        }
        return citationsDir;
    }

    @Override
    protected String getServiceTitle() {
        return "Citation File Upload";
    }

    private List<String> getUnlinkedCitations() throws DAOException {
        Set<String> existing = new HashSet<String>();
        for (File f : citationDirectory.listFiles()) {
            if (f.isFile()) {
                existing.add(f.getName());
            }
        }
        RowIterator citations = dao(Citations.class).read();
        while (citations.hasNext()) {
            String file = citations.next().string(Citations.FILE);
            if (file.trim().length() > 0) {
                existing.remove(file);
            }
        }
        ArrayList<String> sorted = new ArrayList<String>(existing);
        Collections.sort(sorted);
        return sorted;
    }

    @Override
    protected ResourceTemplate buildTemplate() throws DAOException, TemplateException {
        SimpleHash model = new SimpleHash();
        SimpleHash atts = new SimpleHash();
        atts.put("inputFile", TemplateModels.createField("inputFile", "Input File"));
        List<String> unlinked = getUnlinkedCitations();
        Parameter[] params = new Parameter[unlinked.size()];
        for (int i = 0; i < unlinked.size(); i++) {
            params[i] = new Parameter(unlinked.get(i), unlinked.get(i));
        }
        atts.put("existingFile", TemplateModels.createField("existingFile", "Existing File", params));
        model.put("atts", atts);
        model.put("id", getRequest().getAttributes().get("id"));
        model.put("citation", citation.string(Citations.NAME));
        return ResourceTemplate.get("citation-file-form.html", "form", model);
    }
}
