package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.TableDefinition;

public class ProductPage extends DAO {

    private static final TableDefinition columns = new TableDefinition("productpage");
    public static final Column<String> LOCATION = columns.string("location");
    public static final Column<String> STATIONNAME = columns.string("stationname");
    public static final Column<String> STATIONCLASS = columns.string("stationclass");
    public static final Column<String> REGION = columns.string("region");
    public static final Column<String> PROCESS = columns.string("process");
    public static final Column<Integer> NAMEID = columns.integer("nameid");
    public static final Column<Integer> QUALITY = columns.integer("quality");
    public static final Column<String> PERSON = columns.string("person");
    public static final Column<String> EMAIL = columns.string("email");

    public Row getProductPage(String productLocation) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), LOCATION.pair(productLocation));
        Row row = null;
        if (rows.hasNext()) {
            row = rows.next();
        }
        return row;
    }
}
