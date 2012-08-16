
package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.TableDefinition;
import freemarker.template.utility.Collections12;
import java.util.Collections;

public class Citations extends DAO {

    private static final TableDefinition columns = new TableDefinition("citations");
    public static final Column<Integer> ID = columns.integer("id");
	public static final Column<String> NAME = columns.string("name");
	public static final Column<String> AUTHOR = columns.string("author");
	public static final Column<String> YEAR = columns.string("year");
	public static final Column<String> JOURNAL = columns.string("journal");
	public static final Column<String> REGION = columns.string("region");
	public static final Column<String> TOPIC = columns.string("topic");
	public static final Column<String> THEME = columns.string("theme");
	public static final Column<String> CITATION = columns.string("citation");
	public static final Column<String> LINK = columns.string("link");
	public static final Column<String> FILE = columns.string("file");

    public void delete(int id) throws DAOException {
        Pair p = ID.pair(id);
        executeDelete(deleteFromWhere(columns, p),Collections.singletonList(p));
    }

    public Integer addCitation(Pair... pairs) throws DAOException {
        Pair id = null;
        for (Pair p : pairs) {
            if (p.column() == ID) {
                id = p;
                break;
            }
        }
        if (id == null) {
            return (Integer) insert(columns.table(), ID, pairs);
        } else {
            updateWhereEquals(columns.table(), id, pairs);
            return (Integer) id.value();
        }
    }

    public RowIterator read() throws DAOException {
        return readRows(selectAllFrom(columns.table()).orderBy(YEAR));
    }

    public Row read(int id) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), ID.pair(id));
        return rows.hasNext() ? rows.next() : null;
    }

    public void setFile(int id,String file) throws DAOException {
        updateWhereEquals(columns.table(), ID.pair(id), FILE.pair(file.toString()));
    }
}

