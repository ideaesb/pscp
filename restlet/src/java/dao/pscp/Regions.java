
package dao.pscp;

import dao.Column;
import dao.TableDefinition;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.RowIterator;

/**
 *
 * @author iws
 */
public class Regions extends DAO {
    private static final TableDefinition columns = new TableDefinition("regions");
    public static final Column<String> CODE = columns.string("regioncode", "Code");
    public static final Column<String> NAME = columns.string("regionname", "Name");

    public RowIterator readRegions() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public void write(String code, String name) throws DAOException {
        Pair codePair = CODE.pair(code);
        Pair namePair = NAME.pair(name);
        RowIterator existing = selectAllFromWhere(columns.table(), codePair);
        if (existing.hasNext()) {
            updateWhereEquals(columns.table(), codePair, namePair);
        } else {
            insert(columns.table(), codePair,  namePair);
        }
    }
}
