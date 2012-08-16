
package pscp.restlet.util;

import dao.Column;
import dao.TableDefinition;
import dao.Pair;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.restlet.data.Form;

/**
 *
 * @author iws
 */
public class RestletDAOUtils {

    public static List<Pair> parseForm(Form form,TableDefinition cols) {
        ArrayList<Pair> pairs = new ArrayList<Pair>();
        for (String s: form.getNames()) {
            Column col = cols.find(s);
            if (col != null) {
                pairs.add(Pair.of(col, form.getFirstValue(s)));
            }
        }
        return pairs;
    }
    public static List<Pair> parseForm(Form form,Column... cols) {
        return parseForm(form,Arrays.asList(cols));
    }
    public static List<Pair> parseForm(Form form,List<Column> cols) {
        ArrayList<Pair> pairs = new ArrayList<Pair>();
        Set<String> formNames = form.getNames();
        Set<Column> notProcessed = new HashSet<Column>(cols);
        for (String s: formNames) {
            Column col = null;
            for (int i = 0; i < cols.size(); i++) {
                if (cols.get(i).columnName().equals(s)) {
                    col = cols.get(i); break;
                }
            }
            if (col != null) {
                Object val = null;
                String value = form.getFirstValue(s);
                if (Boolean.class == col.type()) {
                    val = Boolean.valueOf("on".equals(value));
                } else {
                    val = col.parse(value);
                }
                pairs.add(Pair.of(col, val));
                notProcessed.remove(col);
            }
        }
        // checkboxes don't send any value back when they're not checked, so
        // consider these to be false
        for (Column col: notProcessed) {
            if (col.type() == Boolean.class) {
                pairs.add(col.pair(Boolean.FALSE));
            }
        }
        return pairs;
    }
}
