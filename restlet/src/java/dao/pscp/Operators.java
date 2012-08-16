package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.RowIterator;
import dao.Rows;
import dao.TableDefinition;
import java.util.Map;

public class Operators extends DAO {

    private static final TableDefinition columns = new TableDefinition("operators");
    public static final Column<Integer> ONUM = columns.integer("onum");
    public static final Column<String> OPERATOR = columns.string("operator");

    public int insertOperator(String name) throws DAOException {
        Number id = (Number) insert(columns.table(), ONUM, OPERATOR.pair(name));
        return id.intValue();
    }

    public Map<String, Integer> readReverseLookup() throws DAOException {
        return Rows.lookup(read(), OPERATOR, ONUM);
    }

    public Map<Integer, String> readLookup() throws DAOException {
        return Rows.lookup(read(), ONUM, OPERATOR);
    }

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns));
    }
}

