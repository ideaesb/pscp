package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.Rows;
import dao.TableDefinition;
import java.util.Date;
import java.util.Map;

public class DataSets extends DAO {

    private static final TableDefinition columns = new TableDefinition("datasets");
    public static final Column<Integer> DATASETID = columns.integer("datasetid");
    public static final Column<String> SOURCE = columns.string("source");
    public static final Column<String> SOURCEID = columns.string("sourceid");
    public static final Column<Date> LASTUPDATED = columns.date("lastupdated");
    public static final Column<String> METADATAURL = columns.string("metadataurl");
    public static final Column<Boolean> COMPLIANT = columns.bool("compliant");
    public static final Column<Boolean> DERIVED = columns.bool("derived");
    public static final Column<Integer> QUALITY = columns.integer("quality");

    public int insert(String source,String location,Date lastUpdated,String metadata,Boolean compliant,boolean derived,int quality) throws DAOException {
        if (lastUpdated == null) {
            lastUpdated = new Date();
        }
        return insert(
                SOURCE.pair(source),
                SOURCEID.pair(location),
                LASTUPDATED.pair(lastUpdated),
                METADATAURL.pair(metadata),
                COMPLIANT.pair(compliant),
                DERIVED.pair(derived),
                QUALITY.pair(quality)
                );
    }

    public Map<Integer, String> readNameLookup() throws DAOException {
        return Rows.lookup(read(), DATASETID, SOURCEID);
    }

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public Row read(int id) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), DATASETID.pair(id));
        Row found = null;
        if (rows.hasNext()) {
            found = rows.next();
        }
        rows.close();
        return found;
    }

    public int insert(Pair... pairs) throws DAOException {
        Number id = (Number) insert(columns.table(), DATASETID, pairs);
        return id.intValue();
    }

    public void update(int id,Pair... fields) throws DAOException {
        updateWhereEquals(columns.table(), DATASETID.pair(id), fields);
    }
}
