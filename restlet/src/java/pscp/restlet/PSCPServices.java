package pscp.restlet;

import dao.DAOCollection;
import dao.DAOException;
import javax.swing.event.ChangeEvent;
import pscp.restlet.util.Passwords;
import pscp.restlet.resource.ContactService;
import dao.DAOFactory;
import dao.Row;
import dao.pscp.Contacts;
import dao.pscp.postgres.PGDaoFactory;
import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import javax.sql.DataSource;
import javax.swing.event.ChangeListener;
import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Finder;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.CookieSetting;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Resource;
import org.restlet.util.ByteUtils;
import org.restlet.util.Resolver;
import org.restlet.util.RouteList;
import org.restlet.util.Series;
import org.restlet.util.Template;
import org.restlet.util.Variable;
import pscp.restlet.resource.AutoCompleteResource;
import pscp.restlet.resource.BaseResource;
import pscp.restlet.resource.BatchMonitorResource;
import pscp.restlet.resource.CitationFileForm;
import pscp.restlet.resource.CitationUploadForm;
import pscp.restlet.resource.ContactForm;
import pscp.restlet.resource.ProductForm;
import pscp.restlet.resource.ProductLinkService;
import pscp.restlet.resource.ProductService;
import pscp.restlet.resource.DirectoryService;
import pscp.restlet.resource.LibraryResource;
import pscp.restlet.resource.ModifiedCache;
import pscp.restlet.resource.ProductPageService;
import pscp.restlet.resource.ProductRevisionForm;
import pscp.restlet.resource.ProductRevisionService;
import pscp.restlet.resource.ProductTypeService;
import pscp.restlet.resource.ProductUploadForm;
import pscp.restlet.resource.StationForm;
import pscp.restlet.resource.StationLocationService;
import pscp.restlet.resource.StationOverviewService;
import pscp.restlet.resource.StationPageService;
import pscp.restlet.resource.StationService;
import pscp.restlet.resource.StationUploadForm;
import pscp.restlet.util.LookupCache;

/**
 *
 * @author iws
 */
public class PSCPServices extends Application {

    private DataSource dataSource;
    private DAOFactory daoFactory;
    private SecretResolver secretResolver;
    private List<String> baseURIs;
    private LookupCache lookupCache;
    private URLS urls;
    private String adminPage;

    public PSCPServices() {
        
    }

    @Override
    public synchronized void start() throws Exception {
        // restlet servlet engine will pass parameters from web.xml via the context parameters
        Series<Parameter> parameters = getContext().getParameters();
        dataSource = PSCPDataSource.createDataSource(parameters);
        daoFactory = PGDaoFactory.createPGDaoFactory(dataSource);
        lookupCache = new LookupCache(daoFactory);
        secretResolver = new SecretResolver();
        adminPage = parameters.getFirstValue("pscp.web.admin");
        urls = new URLS(adminPage);
        // links out to website
        urls.putStatic(new Template(getSlashedURL(parameters,"pscp.web.products")),
                URLS.Name.PRODUCT_LOCATION);
        urls.putStatic(new Template(getSlashedURL(parameters,"pscp.web.root")),
                URLS.Name.STATIC_MEDIA);

        // @todo how to deal with this?
        baseURIs = new ArrayList<String>(Arrays.asList("localhost:8080"));

        Map<String,Object> contextAttributes = getContext().getAttributes();
        contextAttributes.put(BaseResource.CONTEXT_ATTRIBUTE_DAO_FACTORY, daoFactory);
        contextAttributes.put(BaseResource.CONTEXT_ATTRIBUTE_URL_MAPPER, urls);
        contextAttributes.put(BaseResource.CONTEXT_ATTRIBUTE_LOOKUP_CACHE, lookupCache);
        contextAttributes.put(BaseResource.CONTEXT_ATTRIBUTE_PRODUCT_ROOT, 
                getRootDir(BaseResource.CONTEXT_ATTRIBUTE_PRODUCT_ROOT, "pscp-products"));
        contextAttributes.put(BaseResource.CONTEXT_ATTRIBUTE_UPLOAD_ROOT,
                getRootDir(BaseResource.CONTEXT_ATTRIBUTE_UPLOAD_ROOT, "pscp-uploads"));

        super.start();
    }


    private final File getRootDir(String key,String defaultName) {
        String uploadRootSpec = getContext().getParameters().getFirstValue(key);
        File root = uploadRootSpec != null ? new File(uploadRootSpec) : null;
        if (root == null || !root.exists()) {
            File old = root;
            root = new File(System.getProperty("java.io.tmpdir"),defaultName);
            getLogger().info("Configured directory root for " + key + " does not exist : " + old +
                    " using " + root.getAbsolutePath());
            root.mkdirs();
            if (!root.exists()) {
                getLogger().severe("Unable to create directory " + key + " at " + root.getAbsolutePath());
                throw new RuntimeException("Error initializing services : " + key + " misconfigured");
            }
        }
        return root;
    }

    private String getSlashedURL(Series<Parameter> params, String key) {
        String url = params.getFirstValue(key);
        if (url.indexOf('?') < 0 && !url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }

    @Override
    public void setContext(Context context) {
        super.setContext(context);

    }

    @Override
    public Restlet createRoot() {

        Router root = new Router(getContext());
        root.setDefaultMatchingMode(Router.BEST);
        root.setDefaultMatchQuery(false);

        RouterBuilder routes = new RouterBuilder(root);

        RootDirectory rootDirectory = new RootDirectory(root);
        root.attach("",guard(rootDirectory)).setMatchingMode(Template.MODE_EQUALS);
        root.attach("/",guard(rootDirectory)).setMatchingMode(Template.MODE_EQUALS);
        //root.attach("", new RootDirectory(root)).getTemplate().setMatchingMode(Template.MODE_EQUALS);
        //root.attach("/", new RootDirectory(root)).getTemplate().setMatchingMode(Template.MODE_EQUALS);
        routes.addPage("", DirectoryService.class, null, false);
        routes.addPage("/contacts/form/{id}", ContactForm.class, URLS.Name.CONTACT_FORM);
        routes.addPage("/contacts/form/", ContactForm.class);
        routes.addPage("/contacts", ContactService.class, URLS.Name.CONTACT);

        routes.add("/library", LibraryResource.class);
        routes.addPage("/library/", LibraryResource.class);
        routes.addPage("/library/upload", CitationUploadForm.class);
        routes.addPage("/library/{id}", CitationFileForm.class);

        routes.add("/productpage/{id}",ProductPageService.class).getTemplate().getDefaultVariable().setType(Variable.TYPE_ALL);
        routes.addPage("/products/revisions/form/{id}/{rev}", ProductRevisionForm.class, URLS.Name.PRODUCT_REVISIONS_FORM);
        routes.addPage("/products/revisions/form/{id}/", ProductRevisionForm.class);
        routes.addPage("/products/revisions/{id}/{rev}", ProductRevisionService.class, URLS.Name.PRODUCT_REVISIONS);
        routes.addPage("/products/revisions/{id}/", ProductRevisionService.class);
        routes.addPage("/products/form/{id}", ProductForm.class, URLS.Name.PRODUCT_FORM);
        routes.addPage("/products/form/", ProductForm.class);
        routes.addPage("/products/upload", ProductUploadForm.class);
        routes.add("/products", ProductService.class, URLS.Name.PRODUCT);

        routes.add("/productnames/ac", AutoCompleteResource.ProductName.class, URLS.Name.AC_PRODUCT_NAME);

        routes.add("/producttypes", ProductTypeService.class);
        routes.add("/producttypes/ac", AutoCompleteResource.ProductType.class, URLS.Name.AC_PRODUCT_TYPE);
        root.attach("/producttypes/{id}", ProductTypeService.class);

        routes.add("/stations", StationService.class);
        routes.add("/stations/ac", AutoCompleteResource.Station.class, URLS.Name.AC_STATION);
        routes.add("/stations/types/ac", AutoCompleteResource.StationType.class, URLS.Name.AC_STATION_TYPE);
        routes.add("/stations/locations", StationLocationService.class);
        routes.add("/stations/overview", StationOverviewService.class);
        // @see ext comment in BaseResource
        routes.add("/stations/locations.{ext}", StationLocationService.class);
        routes.add("/stations/page/{id}", StationPageService.class, URLS.Name.STATION_PAGE);
        routes.addPage("/stations/upload",StationUploadForm.class);
        routes.add("/stations/all/products", ProductLinkService.class);
        routes.add("/stations/{id}/products", ProductLinkService.class, URLS.Name.PRODUCT_LINK);
        routes.add("/stations/{id}", StationService.class, URLS.Name.STATION);
        routes.add("/stations/", StationService.class);
        routes.addPage("/stations/form/{id}", StationForm.class, URLS.Name.STATION_FORM);
        routes.addPage("/stations/form/", StationForm.class);
        routes.addPage("/batches",BatchMonitorResource.class);

        routes.add("/nations/ac", AutoCompleteResource.Nation.class, URLS.Name.AC_NATIONS);
        routes.add("/regions/ac", AutoCompleteResource.Region.class);
        root.attach("/admin/debug",guard(new DebugRestlet()));
        root.attach("/admin/refresh",guard(new Restlet() {
            public static final String PSCP_DESCRIPTION = "Refresh internal DB and Modified-Since caches";
            @Override
            public void handle(Request request, Response response) {
                getLogger().info("Refresh issued");
                lookupCache.refreshAll();
                ModifiedCache.clear();
                response.setEntity("Refresh Complete", MediaType.TEXT_HTML);
            }

        }));

        root.attach("/login", guard(new Restlet() {
            public static final String PSCP_DESCRIPTION = "Provide Login Features";
            @Override
            public void handle(Request request, Response response) {
                if (request.getChallengeResponse() != null) {
                    Reference referrer = request.getReferrerRef();
                    String ret = request.getOriginalRef().getQueryAsForm().getFirstValue("ret");
                    if (ret != null) {
                        response.redirectSeeOther(ret);
                    } else if (referrer != null) {
                        response.redirectSeeOther(referrer);
                    }
                }
            }
        }));
        urls.put(root.attach("/logout", new Restlet() {
            public static final String PSCP_DESCRIPTION = "Provide Logout Features";
            @Override
            public void handle(Request request, Response response) {
                if (request.getChallengeResponse() != null) {
                    String userName = request.getChallengeResponse().getParameters().getFirstValue("username");
                    if ("logout".equals(userName)) {
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity("Logged Out", MediaType.ALL);
                        CookieSetting cookie = new CookieSetting("u", "");
                        cookie.setPath("/");
                        response.getCookieSettings().add(cookie);
                        response.redirectSeeOther(urls.getURL(URLS.Name.STATIC_MEDIA));
                    } else {
                        response.setStatus(Status.SUCCESS_OK);
                        response.setEntity("Logged Out", MediaType.ALL);
                        CookieSetting cookie = new CookieSetting("u", "");
                        cookie.setPath("/");
                        response.getCookieSettings().add(cookie);
                    }
                } else {
                    ChallengeRequest cr = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST, "PSCP");
                    response.setChallengeRequest(cr);
                    response.setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            }
        }).getTemplate(), URLS.Name.LOGOUT);

        return new HeaderFilter(getContext(), root);
    }

    private Guard createGuard() {
        Guard guard = new Guard(getContext(), "PSCP", baseURIs, "jjaahh") {

            @Override
            public boolean checkSecret(Request request, String identifier, char[] secret) {
                if ("logout".equals(identifier)) {
                    getLogger().log(Level.INFO,"Invalid login attempt for account '" + identifier + "'");
                    return false;
                }
                return super.checkSecret(request, identifier, secret);
            }

            @Override
            public void accept(Request request, Response response) {
                super.accept(request, response);
                String user = request.getChallengeResponse().getParameters().getFirstValue("username");
                if (user != null) {
                    String cookieVal = request.getCookies().getFirstValue("u");
                    if (cookieVal == null || cookieVal.length() == 0) {
                        UUID id = findContactID(user);
                        if (id != null) {
                            setCookie(id, response);
                            request.getAttributes().put(Contacts.class.getName(), id.toString());
                            trackLogin(id);
                            lookupCache.daoModified(Contacts.class);
                        } else {
                            getLogger().warning("Cannot find db entry for user : " + user);
                            response.setStatus(Status.SERVER_ERROR_INTERNAL);
                        }
                    }
                }
            }

            private UUID findContactID(String username) {
                //@todo the penalty may be harsh iteratively searching users, though
                // this should only happen for non-cookie using clients
                Map<String, Row> contacts = lookupCache.CONTACTS.getValue();
                UUID id = null;
                for (Row r : contacts.values()) {
                    if (r.value(Contacts.PERSON).equals(username)) {
                        // @BUG - UUID from postgres returned as PGobject
                        // this needs to be fixed somehow either genericly (if possible)
                        //  or through a "workaround" plugin for Postgres
                        // Workaround is to use string instead of value
                        id = UUID.fromString(r.string(Contacts.CONTACTID));
                        break;
                    }
                }
                return id;
            }

            @Override
            public void forbid(Response response) {
                super.forbid(response);
                setCookie(null, response);
            }

            @Override
            public void challenge(Response response, boolean stale) {
                super.challenge(response, stale);
            }

            private void setCookie(UUID user, Response response) {
                CookieSetting cookie = new CookieSetting("u", user == null ? "" : user.toString());
                cookie.setPath("/");
                response.getCookieSettings().add(cookie);
            }

            private void trackLogin(UUID user) {
                DAOCollection daos = null;
                try {
                    daos = daoFactory.create();
                    daos.get(Contacts.class).login(user);
                } catch (DAOException ex) {
                    getLogger().log(Level.SEVERE, "Error tracking login", ex);
                } finally {
                    if (daos != null) {
                        daos.close();
                    }
                }
            }
        };
        guard.setSecretResolver(secretResolver);
        return guard;
    }

    private Guard guard(Restlet resource) {
        Guard guard = createGuard();
        guard.setNext(resource);
        return guard;
    }

    private Guard guard(Class<? extends Resource> resource) {
        Guard guard = createGuard();
        guard.setNext(resource);
        return guard;
    }

    // @todo Keep this?
    static class HeaderFilter extends Filter {

        HeaderFilter(Context context, Restlet next) {
            super(context, next);
        }

        @Override
        protected void afterHandle(Request request, Response response) {
            Form headers = (Form) response.getAttributes().get("org.restlet.http.headers");
            if (headers == null) {
                response.getAttributes().put("org.restlet.http.headers", headers = new Form());
            }
            Long start = (Long) request.getAttributes().get("start.request.time");
            long processTime = System.currentTimeMillis() - start;
            headers.add("CacheMiss", start + " " + processTime);

            headers = (Form) request.getAttributes().get("org.restlet.http.headers");

            /*
            headers.add("Cache-Control", "max-age=3600");
            long time = new Date().getTime() + 3600000;
            if (response.getEntity() != null) {
            if (response.getEntity().getModificationDate() == null) {
            response.getEntity().setModificationDate(new Date(time - 7200000));
            }
            if (response.getEntity().getExpirationDate() == null) {
            response.getEntity().setExpirationDate(new Date(time));
            }
            }
             */
        }

        @Override
        protected int beforeHandle(Request request, Response response) {
            request.getAttributes().put("start.request.time", System.currentTimeMillis());
            return Filter.CONTINUE;
        }
    }

    class SecretResolver extends Resolver<char[]> implements ChangeListener {

        private Map<String, char[]> secrets = new HashMap<String, char[]>();

        public SecretResolver() {
            lookupCache.CONTACTS.addListener(this);
            load();
        }

        @Override
        public char[] resolve(String key) {
            return secrets.get(key);
        }

        private void load() {
            secrets.clear();
            Map<String, Row> contacts = lookupCache.CONTACTS.getValue(true);
            for (Row contact : contacts.values()) {
                String name = contact.string(Contacts.PERSON);
                String password = contact.string(Contacts.PASSWORD);
                // if password is null or empty, do not add to secrets list!
                if (password != null && password.trim().length() > 0) {
                    getLogger().info("enabling user " + name + ", hash '" + password + "'");
                    try {
                        secrets.put(
                                name,
                                Passwords.decode(password).toCharArray());
                    } catch (Exception ex) {
                        throw new RuntimeException("could not decode password", ex);
                    }
                } 
            }
        }

        public void stateChanged(ChangeEvent e) {
            load();
        }
    }

    class RouterBuilder {

        Router root;

        RouterBuilder(Router root) {
            this.root = root;
        }

        public void addService(Class<? extends Resource> clazz, URLS.Name namedPath, String path,
                String... paths) {
            add(path, clazz, namedPath);
            for (int i = 0; i < paths.length; i++) {
                add(paths[i], clazz);
            }
            addPage(path, clazz);
        }

        public void addServiceWithForm(Class<? extends Resource> clazz, URLS.Name namedPath, String path,
                String... paths) {
            addService(clazz, namedPath, path, paths);

        }

        public Route add(String path, Class<? extends Resource> clazz) {
            return add(path, clazz, null);
        }

        public Route add(String path, Class<? extends Resource> clazz, URLS.Name namedPath) {
            Route route = root.attach(path, clazz);
            route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
            if (namedPath != null) {
                urls.put(route.getTemplate(), namedPath);
            }
            return route;
        }

        public Route addPage(String path, Class<? extends Resource> clazz, URLS.Name namedPath, boolean guard) {
            Route route;
            path = "/" + adminPage + path;
            if (guard) {
                route = root.attach(path, guard(clazz));
            } else {
                route = root.attach(path, clazz);
            }
            route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
            if (namedPath != null) {
                urls.put(route.getTemplate(), namedPath);
            }
            return route;
        }

        public Route addPage(String path, Class<? extends Resource> clazz) {
            return addPage(path, clazz, null, true);
        }

        public Route addPage(String path, Class<? extends Resource> clazz, URLS.Name namedPath) {
            return addPage(path, clazz, namedPath, true);
        }
    }

    class DebugRestlet extends Restlet {

        @Override
        public void handle(Request request, Response response) {
            final StringBuilder sb = new StringBuilder();
            sb.append("<div>");
            append(sb,"static media",urls.getURL(URLS.Name.STATIC_MEDIA));
            append(sb,"product location",urls.getURL(URLS.Name.PRODUCT_LOCATION));
            sb.append("</div>");
            response.setEntity(sb.toString(), MediaType.TEXT_HTML);
        }

        private void append(StringBuilder sb,String name,String val) {
            sb.append("<div>").append(name).append(":'").append(val).append("'</div>");
        }
    }

    static class RootDirectory extends Restlet {

        private final Router router;

        public RootDirectory(Router router) {
            this.router = router;
        }

        @Override
        public void handle(Request request, Response response) {
            final StringBuilder sb = new StringBuilder();
            final RouteList routers = router.getRoutes();
            final String prefix = request.getOriginalRef().getPath().endsWith("/") ? "" : request.getOriginalRef().getPath() + "/";
            final boolean detailed = request.getOriginalRef().getQueryAsForm().getNames().contains("describe");
            sb.append("<div>");
            if (detailed) {
                String href = request.getOriginalRef().getPath();
                sb.append("<a href='" + href + "'>[Less Detail]</a>");
            } else {
                sb.append("<a href='?describe'>[More Detail]</a>");
            }
            sb.append("<ul>");
            for (int i = 0; i < routers.size(); i++) {
                Route r = routers.get(i);
                if (!detailed && r.getTemplate().getVariableNames().size() > 0) {
                    continue;
                }
                String pattern = r.getTemplate().getPattern();
                if (pattern.length() > 0) {
                    pattern = pattern.substring(1);
                    if (pattern.length() > 0) {
                        sb.append("<li>");
                        sb.append("<a href='" + prefix + pattern + "'>" + pattern + "</a>");
                        if (detailed) {
                            Restlet next = r.getNext();
                            Class dest = null;
                            if (next instanceof Finder) {
                                dest = ((Finder) next).getTargetClass();
                            } else if (next instanceof Guard) {
                                Guard g = (Guard) next;
                                if (g.getNext() instanceof Finder) {
                                    dest = ((Finder) g.getNext()).getTargetClass();
                                } else {
                                    dest = g.getNext().getClass();
                                }
                            }
                            if (dest != null) {
                                sb.append("<span> [").append(dest.getName()).append(" ]</span>");
                            }
                            try {
                                String description = findDescription(dest);
                                if (description != null) {
                                    sb.append("<div>").append(description).append("</div>");
                                }
                            } catch (Exception ex) {
                                getLogger().log(Level.SEVERE, "Error getting description", ex);
                            }
                        }
                        sb.append("</li>");
                    }
                }
            }
            sb.append("</ul>");
            sb.append("PSCP API, Version "+Version.getVersion() + ", Created " + Version.getVersionDate());
            sb.append("</div>");
            response.setEntity(sb.toString(), MediaType.TEXT_HTML);
        }

        private String findDescription(final Class dest) throws Exception {
            Class c = dest;
            String description = null;
            while (c != null) {
                Field[] f = c.getFields();
                for (int i = 0; i < f.length; i++) {
                    if (f[i].getName().equals("PSCP_DESCRIPTION")) {
                        description = (String) f[i].get(null);
                        break;
                    }
                }
                c = c.getSuperclass();
            }
            if (dest != null && description == null) {
                InputStream in = dest.getResourceAsStream(dest.getSimpleName() + ".html");
                if (in != null) {
                    description = ByteUtils.toString(in);
                }
            }
            return description;
        }
    }
}
