package dao.pscp;

import dao.Column;
import dao.TableDefinition;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.RowIterator;
import dao.SQLWriter;
import java.util.UUID;

/**
 *
 * @author iws
 */
public class Products extends DAO {

    private static final TableDefinition columns = new TableDefinition("products");
    public static final Column<Integer> ID = columns.integer("id");
    public static final Column<String> CLASS = columns.string("class");
    public static final Column<String> TIMEFRAME = columns.string("timeframe");
    public static final Column<Integer> STATIONID = columns.integer("stationid");
    public static final Column<String> SOURCE = columns.string("source");
    public static final Column<UUID> CONTACTID = columns.uuid("contactid");
    public static final Column<Integer> NAMEID = columns.integer("nameid");
    public static final Column<Integer> REV = columns.integer("rev");

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public RowIterator readProduct(int id) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(ID);
        return readRows(sql, ID.pair(id));
    }

    public RowIterator readProductsAtStation(int id) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(STATIONID).orderBy(NAMEID);
        return readRows(sql, STATIONID.pair(id));
    }

    public void updateProduct(int id,Pair... fields) throws DAOException {
        updateWhereEquals(columns.table(), ID.pair(id), fields);
    }

    public Integer insertProduct(String clazz, String timeframe, int station, String source, UUID productContact,
            int typeID, int datasetid, Integer revision) throws DAOException {
        Object id = insert(columns.table(), ID,
                Pair.of(CLASS, clazz),
                Pair.of(TIMEFRAME, timeframe),
                Pair.of(STATIONID, station),
                Pair.of(SOURCE,source),
                Pair.of(CONTACTID,productContact),
                Pair.of(NAMEID,typeID),
                Pair.of(REV,revision)
                );
        return (Integer) id;
    }
}
