package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.TableDefinition;

public class StationData extends DAO {

    private static final TableDefinition columns = new TableDefinition("stationdata");
    public static final Column<Integer> STATIONID = columns.integer("stationid");
    public static final Column<Integer> DATASETID = columns.integer("datasetid");
    public static final Column<String> STATIONNAME = columns.string("stationname");
    public static final Column<String> NATION = columns.string("nation");
    public static final Column<String> REGION = columns.string("region");
    public static final Column<String> PROCESS = columns.string("process");
    public static final Column<Integer> ONUM = columns.integer("onum");
    public static final Column<String> SOURCEID = columns.string("sourceid");

    public Row find(Pair... values) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), values);
        Row found = null;
        if (rows.hasNext()) {
            found = rows.next();
        }
        rows.close();
        return found;
    }
}
