
package dao;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author iws
 */
public abstract class Columns {
    public static List<Column> filter(List<Column> cols,Column[] columnsToHide) {
        if (columnsToHide != null && columnsToHide.length > 0) {
            cols = new ArrayList<Column>(cols);
            HashSet<String> hide = new HashSet<String>();
            for (Column c: columnsToHide) {
                hide.add(c.columnName());
            }
            Iterator<Column> it = cols.iterator();
            while (it.hasNext()) {
                if (hide.contains(it.next().columnName())) {
                    it.remove();
                }
            }
        }
        return cols;
    }
}
