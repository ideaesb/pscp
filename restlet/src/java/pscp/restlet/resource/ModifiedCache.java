
package pscp.restlet.resource;

import dao.DAO;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.restlet.resource.Resource;

/**
 *
 * @author iws
 */
public class ModifiedCache {

    private static Map<Class<? extends Resource>,Date> modified = new HashMap<Class<? extends Resource>,Date>();

    public static Date getLastModified(Resource r) {
        Date mod = modified.get(r.getClass());
        if (mod == null) {
            long roundedTime = System.currentTimeMillis() / 1000;
            roundedTime *= 1000;
            modified.put(r.getClass(), mod = new Date(roundedTime));
        }
        return mod;
    }

    public static void clear() {
        modified.clear();
    }

    public static void modified(Class<? extends Resource> r) {
        modified.put(r, new Date());
    }

    public static void modified(Resource r) {
        modified.put(r.getClass(), new Date());
    }
}
