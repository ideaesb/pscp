package dao.pscp;

import dao.Column;
import dao.TableDefinition;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.Query;
import dao.Row;
import dao.RowIterator;
import dao.SQLWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author iws
 */
public class ProductNames extends DAO {

    public static final TableDefinition columns = new TableDefinition("productnamecnt");
    public static final Column<Integer> NAMEID = columns.integer("nameid", "ID");
    public static final Column<Integer> TYPEID = columns.integer("typeid", "Type ID");
    public static final Column<String> PROCESS = columns.string("process");
    public static final Column<String> QUADRANT = columns.string("quadrant");
    public static final Column<String> SEASON = columns.string("season");
    public static final Column<String> PERIOD = columns.string("period");
    public static final Column<String> TERM = columns.string("term");
    public static final Column<String> SUBJECT = columns.string("subject");
    public static final Column<String> ATTRIBUTE = columns.string("attribute");
    public static final Column<String> INDICE = columns.string("indice");
    public static final Column<String> STATISTIC = columns.string("statistic");
    public static final Column<Integer> CNT = columns.integer("cnt", "Number of Products");
    
    private static Column[] order = new Column[]{
        PROCESS,
        QUADRANT,
        PERIOD,
        SUBJECT,
        ATTRIBUTE,
        SEASON,
        TERM,
        STATISTIC,
        INDICE
    };

    public Map<Integer, String> readTypeNameLookup() throws DAOException {
        Map<Integer, String> lookup = new HashMap<Integer, String>();
        RowIterator rows = read();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(NAMEID), buildName(r));
        }
        return lookup;
    }
    public Map<String, Row> readReverseTypeNameLookup(Integer group) throws DAOException {
        Map<String, Row> lookup = new HashMap<String, Row>();
        RowIterator rows = readByGroup(group);
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(buildName(r),r);
        }
        return lookup;
    }

    static String buildName(Row r) {
        StringBuilder sb = new StringBuilder();
        for (Column c : order) {
            Object val = r.value(c);
            if (val != null) {
                sb.append(val).append(' ');
            }
        }
        return sb.substring(0, sb.length() - 1);
    }

    public RowIterator search(List<Pair> pairs) throws DAOException {
        return selectAllFromWhere(columns.table(), pairs);
    }

    public RowIterator read(int type) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(NAMEID);
        return readRows(sql, NAMEID.pair(type));
    }

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public RowIterator readByGroup(int group) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(TYPEID).orderBy(NAMEID);
        return readRows(sql, TYPEID.pair(group));
    }
}
