
package dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author iws
 */
public class Tables {
    public static Table defaultTable(List<Column> columns, List<Row> rows) {
        return new DefaultTable(columns,rows);
    }
    public static Table table(List<Table> tables) {
        return table(tables.toArray(new Table[tables.size()]));
    }
    public static <T> List<T> columnValues(Table table,Column<T> col) {
        ArrayList<T> values = new ArrayList<T>(table.rowCount());
        Iterator<Row> it = table.iterator();
        while (it.hasNext()) {
            values.add(it.next().value(col));
        }
        return values;
    }
    public static Table sorted(Table table,final Column<?> col,boolean asc) {
        List<Row> rows = new ArrayList<Row>(table.rowCount());
        for (int i = 0; i < table.rowCount(); i++) {
            rows.add(table.row(i));
        }
        Comparator<Row> comparator;
        if (Comparable.class.isAssignableFrom(col.type())) {
            comparator = new Comparator<Row>() {
                public int compare(Row o1, Row o2) {
                    return ((Comparable) o1.value(col)).compareTo(o2.value(col));
                }
            };
        } else {
            comparator = new Comparator<Row>() {

                public int compare(Row o1, Row o2) {
                    return o1.string(col).compareTo(o2.string(col));
                }
            };
        }
        Collections.sort(rows,comparator);
        return new DefaultTable(table.columns(), rows);
    }

    public static Table table(RowIterator rows) throws DAOException {
        ArrayList<Row> rowlist = new ArrayList<Row>();
        try {
            while (rows.hasNext()) {
                rowlist.add(rows.next());
            }
        } finally {
            rows.close();
        }
        return Tables.defaultTable(rows.columns(), rowlist);
    }

    public static Table table(Table... tables) {
        if (tables.length == 0) {
            return null;
        }
        if (tables.length == 1) {
            return tables[0];
        }
        // @todo column check
        // @todo smarter implementation
        int size = 0;
        for (int i = 0; i < tables.length; i++) {
            size += tables[i].rowCount();
        }
        List<Row> all = new ArrayList<Row>(size);
        for (int i = 0; i < tables.length; i++) {
            for (int j = 0; j < tables[i].rowCount(); j++) {
                all.add(tables[i].row(j));
            }
        }
        return new DefaultTable(tables[0].columns(),all);
    }

    static class DefaultTable implements Table {
        private final List<Column> columns;
        private final List<Row> rows;

        public DefaultTable(List<Column> columns, List<Row> rows) {
            this.columns = columns;
            this.rows = rows;
        }

        public List<Column> columns() {
            return columns;
        }

        public int rowCount() {
            return rows.size();
        }

        public Row row(int i) {
            return rows.get(i);
        }

        public Iterator<Row> iterator() {
            return rows.iterator();
        }

        public RowIterator rowIterator() {
            return new DefaultTableIterator(this);
        }
    }

    static class DefaultTableIterator implements RowIterator {
        private final Table table;
        private int idx;

        DefaultTableIterator(Table table) {
            this.table = table;
            idx = 0;
        }
        public List<Column> columns() {
            return table.columns();
        }

        public Row next() throws DAOException {
            return table.row(idx++);

        }

        public void close() {
            // no worries
        }

        public boolean hasNext() throws DAOException {
            return idx < table.rowCount();
        }

    }
}
