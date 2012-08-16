package dao.pscp;

import dao.Column;
import dao.TableDefinition;
import dao.DAO;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author iws
 */
public class ProductTypes extends DAO {

    private static final TableDefinition columns = new TableDefinition("producttypecnt");
    public static final Column<Integer> ID = columns.integer("typeid", "Group ID");
    public static final Column<String> NAME = columns.string("typename", "Product Name");
    public static final Column<String> CLASS = columns.string("class", "Product Class");
    public static final Column<String> NUM = columns.string("typenum", "Product Number");
    public static final Column<Integer> CNT = columns.integer("cnt", "Number of Products");

    public Map<Integer, String> readTypeNameLookup() throws DAOException {
        Map<Integer, String> lookup = new HashMap<Integer, String>();
        RowIterator rows = readGroups();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(ID), buildName(r));
        }
        return lookup;
    }

    public Map<Integer, String> readTypeIDLookup() throws DAOException {
        Map<Integer, String> lookup = new HashMap<Integer, String>();
        RowIterator rows = readGroups();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(ID), r.string(NUM));
        }
        return lookup;
    }

    private String buildName(Row row) {
        return row.string(NAME);
    }

    public RowIterator readGroups() throws DAOException {
        return readRows(selectAllFrom(columns).orderBy(ID));
    }
}
