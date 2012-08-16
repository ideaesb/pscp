/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.Rows;
import dao.pscp.Contacts;
import dao.pscp.ProductRevisions;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.restlet.data.Form;
import org.restlet.ext.fileupload.RestletFileUpload;
import org.restlet.resource.Representation;
import pscp.restlet.util.ImageScaler;

/**
 *
 * @author en
 */
public class ProductRevisionForm extends FormResource {

    String productID;
    String revisionID;
    private final Column COLUMN_FILE = new Column("file", "Product File", String.class);
    private final Column COLUMN_LINK = new Column("link", "Product Link", String.class);

    @Override
    protected void initFormResource() {
        productID = (String) getRequest().getAttributes().get("id");
        revisionID = (String) getRequest().getAttributes().get("rev");
    }

    @Override
    protected String getFormTemplate() {
        return "product-revision-form.html";
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        RowIterator rows = null;
        if (revisionID != null) {
            rows = dao(ProductRevisions.class).readRevision(Integer.parseInt(productID), Integer.parseInt(revisionID));
        }
        return rows;
    }

    @Override
    protected Row createEditingRow() {
        Map<Column, Object> data = new HashMap<Column, Object>();
        data.put(ProductRevisions.ID, productID);
        return Rows.mapRow(data);
    }

    @Override
    protected String objectName() {
        return "Product Revision";
    }

    @Override
    protected Column[] formAtts() {
        List<Column> cols = new ArrayList<Column>();
        cols.add(COLUMN_FILE);
        cols.add(COLUMN_LINK);
        cols.add(ProductRevisions.COMMENT);
        if (isAdminLoggedIn()) {
            cols.add(ProductRevisions.APPROVED);
        }
        return cols.toArray(new Column[cols.size()]);
    }

    @Override
    protected Column[] formHidden() {
        return new Column[]{
                    ProductRevisions.ID,
                    ProductRevisions.REV,};
    }

    @Override
    protected void accept(Representation entity) throws DAOException {
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
            throw new DAOException("Error during upload");
        }
        openTransaction();
        boolean commit = true;
        ProductRevisions dao = dao(ProductRevisions.class);
        String product = fields.getFirstValue(ProductRevisions.ID.columnName());
        String location = uploadedFile.getName();
        String comments = fields.getFirstValue(ProductRevisions.COMMENT.columnName());
        String newID = UUID.randomUUID().toString();
        String ext = location.substring(location.lastIndexOf('.')).toLowerCase();
        String newFile = newID + ext;
        File dest = new File(getProductRoot(), newFile);
        if (dest.exists()) {
            throw new RuntimeException("The impossible happened " + dest.getAbsolutePath() + " already exists");
        }
        UUID submitter = currentUser().value(Contacts.CONTACTID);
        if (true) {
            throw new RuntimeException("@todo FIX ME - Need md5");
        }
        dao.addRevision(Integer.parseInt(product), newFile, location, comments,submitter,null);
        try {
            uploadedFile.write(dest);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error writing uploaded file", ex);
            commit = false;
        }
        ImageScaler scaler = new ImageScaler();
        scaler.setBounds(256, 256);
        scaler.setInput(dest);
        File thumbnail = new File(getProductRoot(), newID + "_scaled.gif");
        scaler.setOutput(thumbnail);
        scaler.setHQMethod(true);
        try {
            scaler.scale();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error creating thumbnail", ex);
            commit = false;
        }
        if (!commit) {
            delete(dest);
            delete(thumbnail);
        }
        closeTransaction(commit);
    }

    private void delete(File dest) {
        if (dest.exists()) {
            if (!dest.delete()) {
                getLogger().warning("Unable to delete upload product on transaction abort : " + dest.getAbsolutePath());
            }
        }
    }
}
