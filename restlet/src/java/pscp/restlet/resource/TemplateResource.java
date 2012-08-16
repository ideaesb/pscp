package pscp.restlet.resource;

import dao.DAOCollection;
import dao.DAOException;
import dao.Row;
import dao.pscp.Contacts;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateException;
import java.io.IOException;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.ResourceTemplate;

/**
 *
 * @author iws
 */
public abstract class TemplateResource extends BaseResource {

    private boolean pageRequest;
    private boolean allowCaching = true;

    protected void setAllowCaching(boolean allow) {
        this.allowCaching = allow;
    }

    protected boolean isPageRequest() {
        return pageRequest;
    }

    protected abstract String getServiceTitle();

    protected void initTemplateResource() {
    }

    @Override
    protected final void initResource() {
        if (!allowCaching || isModified()) {
            if (!enableJSONResponse()) {
                configureVariants(MediaType.TEXT_HTML);
            }
            pageRequest = urls().isPageRequest(getRequest());
            initTemplateResource();
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
        }
    }

    protected abstract ResourceTemplate buildTemplate() throws DAOException, TemplateException;

    private final Representation html() throws IOException, DAOException, TemplateException {
        ResourceTemplate resourceTemplate = buildTemplate();
        SimpleHash page = new SimpleHash();
        page.put("title", getServiceTitle());
        page.put("static", urls().getURL(Name.STATIC_MEDIA));
        Row user = currentUser();
        page.put("adminUser",user == Row.NULL ? Boolean.FALSE : user.value(Contacts.ADMIN));
        resourceTemplate.addModel("page", page);
        Representation rep;
        if (getRequest().getOriginalRef().getQueryAsForm().getFirst("model") != null) {
            rep = templateModelDump(resourceTemplate.buildModel());
        } else {
            rep = resourceTemplate.render(freeMarkerConfig());
        }
        return rep;
    }

    @Override
    public final Representation represent(Variant variant) throws ResourceException {
        Representation rep = null;
        try {
            long time = System.currentTimeMillis();
            getLogger().info("resolve table in " + (System.currentTimeMillis() - time));
            if (variant.getMediaType() == MediaType.TEXT_HTML) {
                rep = html();
            }
        } catch (Exception ex) {
            logAndThrow("TemplateResource: Error representing service", ex);
        } finally {
            close();
        }
        if (rep != null && allowCaching) {
            rep.setModificationDate(ModifiedCache.getLastModified(this));
        }
        return rep;
    }

    public static abstract class ResourceTemplateBuilder {
        private DAOCollection daos;

        public final void setDAOs(DAOCollection daos) {
            this.daos = daos;
        }

        public abstract ResourceTemplate newTemplate();
    }
}
