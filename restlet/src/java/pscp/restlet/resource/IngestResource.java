
package pscp.restlet.resource;

import dao.Row;
import dao.pscp.Contacts;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import pscp.restlet.util.HLogger;
import pscp.restlet.util.Ingester;

/**
 *
 * @author iws
 * 
 * Debugged the ingestion method so that it properly strips the path from the 
 * user-supplied path/file name. [rla]
 */
public abstract class IngestResource extends TemplateResource {

    public IngestResource() {
        setAllowCaching(false);
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    private File getUploadStorageFile(String uploadName) {
        Row currentUser = currentUser();
        File userRoot = new File(getUploadRoot(),currentUser.string(Contacts.CONTACTID));
        userRoot.mkdirs();
        int idx = uploadName.lastIndexOf(File.separator);
        if (idx != -1) {
            uploadName = uploadName.substring(idx+1);
        }
        idx = uploadName.lastIndexOf('.');
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd'T'hh-mm-ss").format(new Date());
        if (idx >= 0) {
            uploadName = uploadName.substring(0,idx) + "-" + timeStamp + uploadName.substring(idx);
        } else {
            uploadName += timeStamp;
        }
        return new File(userRoot,uploadName);
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
        // if user does not provide file through form, size will be zero
        // do null check anyway in case form is messed up
        if (uploadedFile == null) {
            getLogger().warning("No file provided with POST to ingest resource");
        }
        if (uploadedFile == null || uploadedFile.getSize() == 0) {
            getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        
        File tmpFile = getUploadStorageFile(uploadedFile.getName());
        try {
            uploadedFile.write(tmpFile);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error writing uploaded file to temporary storage", ex);
            tmpFile.delete();
            return;
        }
        StringBuilder responseBuffer = new StringBuilder();
        Ingester ingester = buildIngester(fields, tmpFile, responseBuffer);
        ingester.setLogger(getLogger());
        ingester.setDaoFactory(getDAOFactory());
        ingester.setLogFile(new File(tmpFile.getAbsolutePath() + ".log"));
        // error occurred, do nothing
        if (ingester != null) {
            try {
                getLogger().info("beginning ingest of " + tmpFile.getAbsolutePath() + " on behalf of " + currentUser());
                long time = System.currentTimeMillis();
                boolean modified = doIngest(fields,ingester);
                getLogger().info("ingest in " + (System.currentTimeMillis() - time));
                if (modified) {
                    postIngest(ingester);
                }
            } finally {
                uploadedFile.delete();
            }
        }
    }
    protected abstract void postIngest(Ingester ingester);
    protected abstract Ingester buildIngester(Form fields, File uploadedFile, StringBuilder headerMessages);

    private Representation getMessageRepresenation(List<LogRecord> records,final StringBuilder messages) {
        final StringBuilder b = messages == null ? new StringBuilder() : messages;
        final Map<Level,String> clazzes = new HashMap<Level,String>();
        String clazzFmt = " class='%s'";
        clazzes.put(Level.INFO, String.format(clazzFmt, "msginfo"));
        clazzes.put(Level.WARNING, String.format(clazzFmt, "msgwarn"));
        clazzes.put(Level.SEVERE, String.format(clazzFmt, "msgerr"));
        HLogger.RecordVisitor visitor = new HLogger.RecordVisitor() {

            boolean entering;
            public void enter(LogRecord r) {
                b.append("<li" + clazz(r) + ">");
                b.append(r.getMessage());
                b.append("<ul>");
            }

            private String clazz(LogRecord r) {
                String clazz = clazzes.get(r.getLevel());
                if (clazz == null) {
                    clazz = "";
                }
                return clazz;
            }

            public void visit(LogRecord r) {
                b.append("<li" + clazz(r) + ">").append(r.getMessage()).append("</li>");
            }

            public void exit() {
                b.append("</li>");
                b.append("</ul>");
            }
        };
        b.append("<ul>");
        for (LogRecord r : records) {
            HLogger.visit(r, visitor);
        }
        b.append("</ul>");
        return new StringRepresentation(messages, MediaType.TEXT_HTML);
    }

    private boolean doIngest(Form fields,Ingester ingester) {
        boolean complete = false;
        UUID errorCode = UUID.randomUUID();
        List<LogRecord> messages = null;
        Exception error = null;
        boolean dryRun = "on".equals(fields.getFirstValue("dryRun", "off"));
        ingester.setDryRun(dryRun);
        boolean rollback = dryRun;
        try {
            messages = ingester.ingest();
        } catch (Exception ex) {
            error = ex;
            rollback = true;
            getLogger().log(Level.SEVERE, errorCode + " - Error ingesting ", ex);
        }
        try {
            if (rollback) {
                ingester.rollback();
            } else {
                ingester.commit();
                complete = true;
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error committing/rollingback", ex);
        }
        if (error != null) {
            getResponse().setEntity("An error occurred on ingest : " + error.getMessage() + ". Please use the following code to report this: " + errorCode, MediaType.TEXT_HTML);
        } else {
            StringBuilder responseText = new StringBuilder();
            responseText.append("<h2>Upload Successful</h2>");
            if (dryRun) {
                responseText.append("<h3>This is a dry run.</h3>");
            }
            getResponse().setEntity(getMessageRepresenation(messages,responseText));
        }
        return complete;
    }
}
