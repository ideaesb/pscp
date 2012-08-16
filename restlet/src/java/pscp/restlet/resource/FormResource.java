package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Contacts;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public abstract class FormResource extends BaseResource {

    private String template;

    protected abstract void initFormResource();
    protected abstract RowIterator resolveTable() throws DAOException;
    protected abstract String objectName();
    protected abstract Column[] formAtts();
    protected abstract Column[] formHidden();
    protected abstract void accept(Representation entity) throws DAOException;

    protected void setFormTemplate(String template) {
        this.template = template;
    }

    protected String getFormTemplate() {
        return template == null ? "form-generic.html" : template;
    }

    @Override
    protected final void initResource() {
        configureVariants(MediaType.TEXT_HTML);
        initFormResource();
        setModifiable(true);
    }

    protected List<Pair> pairs(Column[] cols, Row editing) {
        return pairs(cols, editing, true);
    }

    protected List<Pair> pairs(Column[] cols, Row editing, boolean maskNull) {
        List<Pair> pairs = new ArrayList<Pair>();
        for (int i = 0; i < cols.length; i++) {
            Object value;
            if (maskNull) {
                value = editing == null ? "" : editing.string(cols[i]);
            } else {
                value = editing == null ? null : editing.value(cols[i]);
                if (value != null) {
                    value = value.toString();
                }
            }
            pairs.add(Pair.of(cols[i], value));
        }
        return pairs;
    }

    protected Representation render(Template template,TemplateModel model) throws IOException,TemplateException {
        StringWriter buf = new StringWriter();
        template.process(model, buf);
        return new StringRepresentation(buf.getBuffer(), MediaType.TEXT_HTML);
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        try {
            accept(entity);
            close();
        } catch (DAOException ex) {
            ex.printStackTrace();
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL, "Error on accepting entity");
        }
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Template template;
        SimpleHash model;
        Row editing = null;
        Representation rep = null;
        try {
            template = getTemplate("form-template.html");
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE,"Unable to load form template - " + getFormTemplate(),ex);
            throw new ResourceException(ex);
        }
        try {
            RowIterator rows = resolveTable();
            if (rows != null) {
                editing = rows.next();
            }
        } catch (DAOException ex) {
            getLogger().log(Level.SEVERE,"Error retrieving data",ex);
            throw new ResourceException(ex);
        }

        if (editing == null) {
            editing = createEditingRow();
        }
        model = adminPageModel( (editing == null ? "Add New" : "Edit") + " " + objectName());
        model.put("contentTemplate", getFormTemplate());
        try {
            model.put("form",getFormModel(editing));
            if (getRequest().getOriginalRef().getQueryAsForm().getNames().contains("model")) {
                rep = templateModelDump(model);
            } else {
                rep = render(template, model);
            }
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE,"Error during templating",ex);
            throw new ResourceException(ex);
        } finally {
            close();
        }
        return rep;
    }

    protected Row createEditingRow() {
        return null;
    }

    protected void completeFormModel(TemplateModels.FormModel form,Row editing) throws DAOException {
        form.setAttributes(pairs(formAtts(), editing));
        form.setHidden(pairs(formHidden(), editing));
    }

    protected TemplateModel getFormModel(Row editing) throws DAOException {
        TemplateModels.FormModel form = new TemplateModels.FormModel();
        form.setURL(getRequest().getOriginalRef().toString());
        completeFormModel(form,editing);
        return form;
    }
}
