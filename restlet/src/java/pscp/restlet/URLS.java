package pscp.restlet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.util.Template;

/**
 *
 * @author iws
 */
public class URLS {
    private Map<Name, Template> templates = new HashMap<Name, Template>();
    private Map<Name, Template> staticRoutes = new HashMap<Name, Template>();
    private final String pageRoot;
    private String productLocationBase;
    private Reference root;
    private List<String> rootSegments;

    public URLS(String pageRoot) {
        this.pageRoot = pageRoot;
    }

    public String getProductThumb(String uri) {
        if (productLocationBase == null) {
            productLocationBase = getURL(Name.PRODUCT_LOCATION);
        }
        uri = productLocationBase + uri;
        int idx = uri.lastIndexOf('.');
        StringBuilder thumbString = new StringBuilder();
        thumbString.append(uri, 0, idx);
        // for now, all scaled products are gifs
        thumbString.append("_scaled.gif");
        return thumbString.toString();
    }

    // hack - allow incomplete link definitions
    public Set<Name> getDefinedLinks() {
        Set<Name> defined = new HashSet<Name>();
        for (Name name : Name.values()) {
            if (templates.containsKey(name)) {
                defined.add(name);
            }
        }
        return defined;
    }

    public Reference getPageReference(Name name, String... vars) {
        List<String> segments = new ArrayList<String>(rootSegments);
        Template template = templates.get(name);
        List<String> pageSegments = new Reference(template.format(varMap(vars))).getSegments();
        if (!pageSegments.get(0).equals(pageRoot)) {
            segments.add(pageRoot);
        }
        segments.addAll(pageSegments);
        Reference ref = new Reference(root);
        ref.setSegments(segments);
        return ref;
    }

    private Reference join(String uri) {
        List<String> segments = new ArrayList(rootSegments);
        segments.addAll(new Reference(uri).getSegments());
        Reference ref = new Reference(root);
        ref.setSegments(segments);
        return ref;
    }

    public Reference getReference(Name name) {
        Template template = templates.get(name);
        Reference ref;
        if (template == null) {
            template = staticRoutes.get(name);
            if (template == null) {
                throw new NullPointerException("no template for " + name);
            }
            ref = new Reference(template.format(Collections.EMPTY_MAP));
        } else {
            ref = join(template.format(Collections.EMPTY_MAP));
        }
        return ref;
    }

    public String getURL(Name name) {
        return getReference(name).toString();
    }

	/*
	 * Given the variable list of arguments in vars, convert it to a Map and return it.
	 *
	 * vars must be in the form of key-value pairs.
	 */
    private Map<String, String> varMap(String... vars) {
        if (vars.length % 2 != 0) {
            throw new RuntimeException("Illegal number of variables " + vars.length);
        }
        Map<String, String> map = new HashMap<String, String>(vars.length / 2);
        for (int i = 0; i < vars.length; i += 2) {
            map.put(vars[i], vars[i + 1]);
        }
        return map;
    }

    public Reference getReference(Name name, String... vars) {
        Map<String, String> map = varMap(vars);
        Template template = templates.get(name);
        Reference ref;
        if (template == null) {
            template = staticRoutes.get(name);
            if (!template.getVariableNames().containsAll(map.keySet())) {
                throw new IllegalArgumentException("template does not contain all variables provided." +
                        map.keySet() + " were provided, but " + template.getVariableNames() + " are available.");
            }
            ref = new Reference(template.format(map));
        } else {
            ref = join(template.format(map));
        }
        return ref;
    }

    public String getURL(Name name, String... vars) {
        return getReference(name, vars).toString();
    }

    void put(Template template, Name name) {
        if (templates.containsKey(name)) {
            throw new IllegalStateException(name + " already exists in URLS : " + templates.get(name));
        }
        templates.put(name, template);
    }

    void putStatic(Template template, Name name) {
        if (staticRoutes.containsKey(name)) {
            throw new IllegalStateException(name + " already exists in URLS");
        }
        staticRoutes.put(name, template);
    }

    public boolean isPageRequest(Request req) {
        List<String> segments = req.getOriginalRef().getSegments();
        segments.removeAll(rootSegments);
        return segments.size() > 0 ? segments.get(0).equals(pageRoot) : false;
    }

    public boolean isRootSet() {
        return root != null;
    }

    public void setRoot(Reference rootRef) {
        root = new Reference(rootRef);
        rootSegments = Collections.unmodifiableList(root.getSegments());
    }

    public static enum Name {
        CONTACT,
        CONTACT_FORM,
        LOGOUT,
        PRODUCT,
        PRODUCT_FORM,
        PRODUCT_LINK,
        PRODUCT_TYPE,
        PRODUCT_LOCATION,
        PRODUCT_REVISIONS,
        PRODUCT_REVISIONS_FORM,
        STATIC_MEDIA,
        STATION,
        STATION_FORM,
        STATION_PAGE,
        AC_NATIONS,
        AC_PRODUCT_TYPE,
        AC_PRODUCT_NAME,
        AC_STATION,
        AC_STATION_TYPE;
        private final String display;

        Name() {
            this(null);
        }

        Name(String display) {
            if (display == null) {
                display = computeDisplayName();
            }
            this.display = display;
        }

        public String displayName() {
            return display;
        }

        private String computeDisplayName() {
            StringBuilder b = new StringBuilder(name().toLowerCase());
            b.setCharAt(0, Character.toUpperCase(b.charAt(0)));
            for (int i = 1; i < b.length(); i++) {
                if (b.charAt(i) == '_') {
                    b.setCharAt(i, ' ');
                    b.setCharAt(i + 1, Character.toUpperCase(b.charAt(i + 1)));
                }
            }
            return b.toString();
        }
    }

    public static void main(String[] args) throws Exception {
        URLS urls = new URLS("pages");
        urls.put(new Template("/contacts/{id}"), Name.CONTACT);
        urls.putStatic(new Template("/contacts/{id}"), Name.PRODUCT);
        urls.setRoot(new Reference("http://boobar/xxx"));
        System.out.println(urls.getURL(Name.CONTACT));
        System.out.println(urls.getURL(Name.CONTACT, "id", "billy"));
        System.out.println(urls.getURL(Name.PRODUCT));
        System.out.println(urls.getPageReference(Name.CONTACT ));
        System.out.println(urls.getPageReference(Name.CONTACT, "id", "billy"));
    }
}
