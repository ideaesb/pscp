package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.RowIterator;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import pscp.restlet.URLS;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.RowTransformer;
import pscp.restlet.util.Representations;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public abstract class ServiceResource extends BaseResource {

    private boolean pageRequest;
    private String template;

    protected boolean isPageRequest() {
        return pageRequest;
    }

    protected void setServiceTemplate(String template) {
        this.template = template;
    }

    protected String getServiceTemplate() {
        return template == null ? "table-generic.html" : template;
    }

    protected abstract void initServiceResource();

    protected abstract String getServiceTitle();

    @Override
    protected final void initResource() {
        if (isModified()) {
            if (!enableJSONResponse()) {
                configureVariants(MediaType.TEXT_HTML);
            }
            pageRequest = urls().isPageRequest(getRequest());
            initServiceResource();
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
        }
    }

    protected abstract RowIterator resolveTable() throws DAOException;

    protected ResourceTemplate buildTemplate(RowIterator rows) {
        return null;
    }

    protected Representation html(RowIterator rows) throws IOException, DAOException, TemplateException {
        Representation rep = null;
        final boolean debugModel = getRequest().getOriginalRef().getQueryAsForm().getFirst("model") != null;
        ResourceTemplate resourceTemplate = buildTemplate(rows);
        if (resourceTemplate == null) {
            Template template;
            SimpleHash model;
            if (isPageRequest()) {
                template = getTemplate("form-template.html");
                model = adminPageModel(getServiceTitle());
                model.put("contentTemplate", getServiceTemplate());
            } else {
                template = getTemplate(getServiceTemplate());
                model = new SimpleHash();
                model.put("webroot",urls().getURL(URLS.Name.STATIC_MEDIA));
            }
            model.put("table", createTemplateModel(rows));
            rep = debugModel ? templateModelDump(model) : render(template, model);
        } else {
            if (isPageRequest()) {
                SimpleHash adminPageModel = adminPageModel(getServiceTitle());
                adminPageModel.put("contentTemplate", getServiceTemplate());
                ResourceTemplate pageTemplate = ResourceTemplate.get("form-template.html", adminPageModel);
                pageTemplate.addTemplate(resourceTemplate);
            }
            rep = debugModel ? templateModelDump(resourceTemplate.buildModel()) : resourceTemplate.render(freeMarkerConfig());
        }
        return rep;
    }

    protected Representation json(RowIterator rows) throws JSONException, DAOException {
        return Representations.tableJSON(rows);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;
        try {
            long time = System.currentTimeMillis();
            RowIterator rows = resolveTable();
            getLogger().info("resolve table in " + (System.currentTimeMillis() - time));
            MediaType media = variant.getMediaType();
            if (media == MediaType.TEXT_HTML) {
                rep = html(rows);
            } else if (media == MediaType.APPLICATION_JSON || media == MediaType.APPLICATION_JAVASCRIPT) {
                rep = json(rows);
            }
        } catch (Exception ex) {
            logAndThrow("ServiceResource: Error representing service. "+ex.toString(), ex);
        } finally {
            close();
        }
        if (rep != null) {
            rep.setModificationDate(ModifiedCache.getLastModified(this));
        }
        return rep;
    }

    protected TemplateModel createTemplateModel(RowIterator rows) throws DAOException {
        SimpleHash table = new SimpleHash();
        table.put("cols", TemplateModels.hashColumns(tableColumns(rows)));
        table.put("rows", TemplateModels.tableModel(rows, getRowTransformer(rows)));
        return table;
    }

    protected List<Column> tableColumns(RowIterator rows) {
        return rows.columns();
    }

    protected RowTransformer getRowTransformer(RowIterator rows) {
        return TemplateModels.defaultRowTransformer(rows.columns());
    }
}
