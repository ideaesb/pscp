package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.RowIterator;
import dao.TableDefinition;

public class Nations extends DAO {

    private static final TableDefinition columns = new TableDefinition("nations");
    public static final Column<String> ISOCODE = columns.string("isocode");
    public static final Column<String> COUNTRYNAME = columns.string("countryname");

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns));
    }
}
