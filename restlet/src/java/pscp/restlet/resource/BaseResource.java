package pscp.restlet.resource;

import com.noelios.restlet.ext.servlet.ServletContextAdapter;
import dao.DAO;
import dao.DAOCollection;
import dao.DAOException;
import dao.DAOFactory;
import dao.Row;
import dao.pscp.Contacts;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.cache.WebappTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import pscp.restlet.URLS;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.util.BackgroundExecutor;
import pscp.restlet.util.LookupCache;

/**
 * Note on extensions -
 * According to REST/Web philosophers, extension "squatting" is not good for the same reasons other
 * types of "squatting" aren't - you never now when a URI extension will accidtentally map to user
 * content.
 * So how does one request a variant of a resource? The spec is actually clear - use headers and all
 * of this works well and good, except that sometimes browsers or other caching occurs based on the
 * URI/URL alone, so adding the extension differentiates the request.
 * This also makes things easier for client authoring.
 * In this application, only the StationLocationService is providing this.
 *
 * @author iws
 */
public abstract class BaseResource extends Resource {
    private static final Map<String, MediaType> mimeResolver = new HashMap<String, MediaType>();
    static {
        mimeResolver.put("html", MediaType.TEXT_HTML);
        mimeResolver.put("json", MediaType.APPLICATION_JSON);
        mimeResolver.put("js", MediaType.APPLICATION_JAVASCRIPT);
    }

    private static Configuration configuration;
    protected static final Variant VARIANT_JSON = new Variant(MediaType.APPLICATION_JSON);
    protected static final Variant VARIANT_HTML = new Variant(MediaType.TEXT_HTML);
    private static Map<Class<? extends DAO>,Set<Class<? extends Resource>>> daoDependencies =
            new HashMap<Class<? extends DAO>,Set<Class<? extends Resource>>>();

    public static final String CONTEXT_ATTRIBUTE_PRODUCT_ROOT = "pscp.product.root";
    public static final String CONTEXT_ATTRIBUTE_DAO_FACTORY = "pscp.dao.factory";
    public static final String CONTEXT_ATTRIBUTE_URL_MAPPER = "pscp.url.mapper";
    public static final String CONTEXT_ATTRIBUTE_LOOKUP_CACHE = "pscp.lookup.cache";
    public static final String CONTEXT_ATTRIBUTE_UPLOAD_ROOT = "pscp.upload.root";
    private static BackgroundExecutor jobExecutor;

    private DAOCollection daos;
    private Row currentUser;
    private Logger logger;

    protected static final void declareDAODependency(Class<? extends Resource> res,Class<? extends DAO>... deps) {
        assert resourceNotDeclaredYet(res);
        for (Class<? extends DAO> c: deps) {
            Set<Class<? extends Resource>> s = daoDependencies.get(c);
            if (s == null) {
                daoDependencies.put(c, s = new HashSet<Class<? extends Resource>>());
            }
            s.add(res);
        }
    }

    private static boolean resourceNotDeclaredYet(Class<? extends Resource> res) {
        boolean declared = false;
        for (Set<Class<? extends Resource>> s: daoDependencies.values()) {
            declared |= s.contains(res);
        }
        return declared;
    }

    protected BackgroundExecutor getJobExecutor() {
        if (jobExecutor == null) {
            jobExecutor = new BackgroundExecutor();
        }
        return jobExecutor;
    }

    protected final void daoModified(Class<? extends DAO> dao) {
        lookup().daoModified(dao);
        Set<Class<? extends Resource>> deps = daoDependencies.get(dao);
        if (deps != null) {
            for (Class<? extends Resource> r: deps) {
                ModifiedCache.modified(r);
            }
        }
    }

    @Override
    public Logger getLogger() {
        // the getLogger implementation of Restlet uses the Context to obtain a logger.
        // This is slower and we don't have flexability for various loggers.
        // The default context logger is an "anonymous" logger that comes with it's own set of issues...
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    protected final File getUploadRoot() {
        return (File) getContext().getAttributes().get(CONTEXT_ATTRIBUTE_UPLOAD_ROOT);
    }

    protected final File getProductRoot() {
        return (File) getContext().getAttributes().get(CONTEXT_ATTRIBUTE_PRODUCT_ROOT);
    }

    protected final DAOFactory getDAOFactory() {
        return (DAOFactory) getContext().getAttributes().get(CONTEXT_ATTRIBUTE_DAO_FACTORY);
    }

    protected final LookupCache lookup() {
        return (LookupCache) getContext().getAttributes().get(CONTEXT_ATTRIBUTE_LOOKUP_CACHE);
    }

    protected final URLS urls() {
        URLS urls = (URLS) getContext().getAttributes().get(CONTEXT_ATTRIBUTE_URL_MAPPER);
        if (!urls.isRootSet()) {
            urls.setRoot(getRequest().getRootRef());
        }
        return urls;
    }

    protected final <T extends DAO> T dao(Class<T> type) throws DAOException {
        if (daos == null) {
            daos = getDAOFactory().create();
        }
        return daos.get(type);
    }

    protected final void openTransaction() throws DAOException {
        if (daos == null) {
            daos = getDAOFactory().create(true);
        } else {
            throw new IllegalStateException("daos already created");
        }
    }

    protected final void closeTransaction(boolean commit) throws DAOException {
        if (commit) {
            daos.commit();
        } else {
            daos.rollback();
        }
        daos.close();
        daos = null;
    }

    protected final void useDAOs(BaseResource resource) throws DAOException {
        daos = resource == null ? null : resource.daos;
    }

    protected final void close() {
        if (daos != null) {
            daos.close();
            daos = null;
        }
    }

    @Override
    protected final void finalize() throws Throwable {
        if (daos != null) {
            getLogger().warning(getClass().getName() + " did not close dao connections!");
            close();
        }
        super.finalize();
    }

    protected final boolean enableJSONResponse() {
        boolean jsonRequested = false;
        List<Preference<MediaType>> accept = getRequest().getClientInfo().getAcceptedMediaTypes();
        for (int i = 0; i < accept.size(); i++) {
            if (accept.get(i).getMetadata() == MediaType.APPLICATION_JSON) {
                jsonRequested = true;
                getVariants().add(VARIANT_JSON);
            }
        }
        return jsonRequested;
    }

    protected final void logAndThrow(String message, Throwable cause) throws ResourceException {
        getLogger().log(Level.SEVERE, message, cause);
        throw new ResourceException(Status.SERVER_ERROR_INTERNAL, message, cause);
    }

    @Override
    public final void init(Context context, Request request, Response response) {
        super.init(context, request, response);
        initResource();
    }

    protected boolean isModified() {
        boolean isModified = true;
        Request req = getRequest();
        if (req.getOriginalRef().getQuery() == null) {
            Date since = req.getConditions().getModifiedSince();
//            Form headers = (Form) req.getAttributes().get("org.restlet.http.headers");
            //@todo check cache-control fields....
            if (since != null) {
                Date modified = ModifiedCache.getLastModified(this);
                isModified = modified.after(since);
            }
        }
        return isModified;
    }

    protected abstract void initResource();

    protected Representation render(Template template, TemplateModel model) throws TemplateException, IOException {
        StringWriter buf = new StringWriter();
        template.process(model, buf);
        return new StringRepresentation(buf.getBuffer(), MediaType.TEXT_HTML);
    }

    protected final Representation templateModelDump(TemplateModel model) throws TemplateModelException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(baos);
        ResourceTemplate.dumpModel(model, out, false);
        out.flush();
        return new StringRepresentation(baos.toString(), MediaType.TEXT_PLAIN);
    }

    protected final Template getTemplate(String name) throws IOException {
        Configuration config = freeMarkerConfig();
        try {
            Template template = config.getTemplate(name);
            /*
            template.setTemplateExceptionHandler(new TemplateExceptionHandler() {

            public void handleTemplateException(TemplateException arg0, Environment arg1, Writer arg2) throws
            TemplateException {
            arg0.printStackTrace();
            getResponse().setStatus(Status.SERVER_ERROR_INTERNAL, "Error templating");
            try {
            arg2.flush();
            arg2.close();
            } catch (IOException ex) {
            Logger.getLogger(BaseResource.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw arg0;
            }
            });
             */
            return template;
        } catch (IOException ioe) {
            System.err.println("template error source is : " + name);
            throw ioe;
        }
    }

    protected final boolean isUserLoggedIn() {
        return currentUser() != Row.NULL;
    }

    protected final boolean isAdminLoggedIn() {
        return currentUser() != Row.NULL && currentUser().value(Contacts.ADMIN);
    }

    protected final Row currentUser() {
        if (currentUser == null) {
            String u = getRequest().getCookies().getFirstValue("u");
            if (u != null) {
                currentUser = lookup().CONTACTS.getValue().get(u);
            } else {
                String uid = (String) getRequest().getAttributes().get(Contacts.class.getName());
                if (uid != null) {
                    currentUser = lookup().CONTACTS.getValue().get(uid);
                }
            }
            if (currentUser == null) {
                currentUser = Row.NULL;
            }
        }
        return currentUser;
    }

    protected final SimpleHash adminPageModel(String title) {
        SimpleHash model = new SimpleHash();
        SimpleHash page = new SimpleHash();
        Row user = currentUser();
        Boolean admin = Boolean.FALSE;
        if (user != null) {
            page.put("user", user.string(Contacts.PERSON));
            admin = user.value(Contacts.ADMIN);
        }
        page.put("admin",admin);
        page.put("currentURL", getRequest().getOriginalRef().toString());
        page.put("title", title);
        model.put("page", page);
        return model;
    }

    protected final Configuration freeMarkerConfig() {
        if (configuration == null) {
            configuration = new Configuration();
            try {
                configuration.setSharedVariable("static", urls().getURL(URLS.Name.STATIC_MEDIA));
                ServletContextAdapter context = (ServletContextAdapter) getContext();
                List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
                String user = System.getProperty("user.name");
                String[] testLocations = new String[]{
                    "C:/rtisw/webapp.pscp/webapp.pscp.restlet/web/templates"
                };
                for (int i = 0; i < testLocations.length; i++) {
                    String loc = testLocations[i];
                    try {
                        loaders.add(new FileTemplateLoader(new File(loc)));
                        getLogger().info("added test template directory at " + loc);
                    } catch (FileNotFoundException fnfe) {
                        getLogger().warning(
                                "Unable to locate developer template directory : " + loc + " - not a problem in production");
                    }
                }

                loaders.add(new WebappTemplateLoader(context.getServletContext(), "templates"));
                MultiTemplateLoader loader = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[0]));
                configuration.setTemplateLoader(loader);
                configuration.setSetting(Configuration.TEMPLATE_UPDATE_DELAY_KEY, "0");
            } catch (Exception ex) {
                Logger.getLogger(BaseResource.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return configuration;
    }

    protected void addCacheControl(int seconds) {
//        Form headers = (Form) getResponse().getAttributes().get("org.restlet.http.headers");
//        if (headers == null) {
//            getResponse().getAttributes().put("org.restlet.http.headers", headers = new Form());
//        }
//        headers.add("Cache-Control", "max-age=" + seconds);
    }

    protected void configureVariants(MediaType defaultType) {
        String path = getRequest().getOriginalRef().getPath();
        int dot = path.lastIndexOf('.');
        String ext = null;
        if (dot >= 0) {
            ext = path.substring(dot + 1);
        }
        if (ext != null) {
            MediaType resolved = mimeResolver.get(ext);
            if (resolved != null) {
                getVariants().add(new Variant(resolved));
            }
        } else {
            getVariants().add(new Variant(defaultType));
        }
    }

    protected String href(String href, String name) {
        if (href == null) {
            return "";
        }
        if (name == null) name = href;
        return "<a href='" + href + "'>" + name + "</a>";
    }

    protected String img(String href) {
        return "<img src='" + href + "'></img>";
    }

    public static String xmlEncode(final String str) {
        if (str == null) {
            return null;
        }
        boolean replaced = false;
        StringBuilder buf = null;
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            boolean controlCharacter = ch < 32;
            boolean unicodeButNotAscii = ch > 126;
            boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>';

            if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
                if (buf == null) {
                    buf = new StringBuilder(str.length());
                    buf.append(str,0,i);
                }
                buf.append("&#" + (int) ch + ";");
                replaced = true;
            } else if (buf != null) {
                buf.append(ch);
            }
        }
        if (replaced == false) {
            return str;
        }
        return buf.toString();
    }

}
