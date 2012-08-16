
package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.RowIterator;
import dao.TableDefinition;

public class Topics extends DAO {

    private static final TableDefinition columns = new TableDefinition("topics");
    public static final Column<String> ID = columns.string("id");
	public static final Column<String> NAME = columns.string("name");

    public RowIterator readTopics() throws DAOException {
        return readRows(selectAllFrom(columns).orderBy(ID));
    }

    public void write(String id, String name) throws DAOException {
        Pair codePair = ID.pair(id);
        Pair namePair = NAME.pair(name);
        RowIterator existing = selectAllFromWhere(columns.table(), codePair);
        if (existing.hasNext()) {
            updateWhereEquals(columns.table(), codePair, namePair);
        } else {
            insert(columns.table(), codePair,  namePair);
        }
    }
}

