package dao;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iws
 */
class RowReader {
    private static void close(ResultSet rs) throws SQLException {
        rs.getStatement().close();
    }

    /**
     * Index the columns that are actually in the result set. It is okay if more columns are
     * provided than actually present.
     */
    private static Map<Column, Integer> index(ResultSetMetaData md, List<Column> cols) throws SQLException {
        Map<Column, Integer> index = new HashMap<Column, Integer>(cols.size());
        // @todo the check on found columns is done backwards!!
        String[] colNames = new String[md.getColumnCount()];
        for (int i = 0; i < md.getColumnCount(); i++) {
            String colName = md.getColumnName(i + 1);
            colNames[i] = colName;
            for (int j = 0; j < cols.size(); j++) {
                Column<?> check = cols.get(j);
                if (check.columnName().equals(colName)) {
                    index.put(check, i);
                    break;
                }
            }
        }
        return Collections.unmodifiableMap(index);
    }

    static List<Column> filter(List<Column> cols, int[] index) {
        List<Column> filtered = new ArrayList<Column>();
        for (int i = 0; i < index.length; i++) {
            if (index[i] >= 0) {
                filtered.add(cols.get(i));
            }
        }
        return filtered;
    }

    public static RowIterator readRows(Class<? extends DAO> spec, ResultSet rs) throws SQLException {
        List<Column> columns = DAOs.columns(spec);
        Map<Column,Integer> index = index(rs.getMetaData(),columns);
        if (index.size() != columns.size()) {
            columns = new ArrayList<Column>(index.size());
            columns.addAll(Collections.nCopies(index.size(), (Column)null));
            for (Map.Entry<Column,Integer> kv : index.entrySet()) {
                columns.set(kv.getValue(), kv.getKey());
            }
        }
        return new ResultSetRowIterator(rs, index(rs.getMetaData(), columns), columns);
    }

    static class ResultSetRowIterator implements RowIterator {
        final List<Column> columns;
        final Map<Column, Integer> index;
        final ResultSet rs;
        final Class[] coltype;
        Row row;

        private ResultSetRowIterator(ResultSet rs, Map<Column, Integer> index, List<Column> columns) throws SQLException {
            this.rs = rs;
            if (index == null) throw new NullPointerException("index");
            if (columns == null) throw new NullPointerException("columns");
            if (index.size() != columns.size()) throw new IllegalArgumentException("columns != index");
            this.index = index;
            this.columns = columns;
            this.coltype = new Class[rs.getMetaData().getColumnCount()];
            for (int i = 0; i < columns.size(); i++) {
                Integer idx = index.get(columns.get(i));
                if (idx != null) {
                    coltype[idx] = columns.get(i).type();
                }
            }
        }

        public List<Column> columns() {
            return columns;
        }

        public Row next() throws DAOException {
            Row next = null;
            if (row == null) {
                fetch();
            }
            next = row;
            row = null;
            return next;
        }

        private Object getValue(ResultSet rs, int idx) throws SQLException {
            Object value = null;
            Class type = coltype[idx];
            final int rsidx = idx + 1;
            if (type == Integer.class) {
                value = Integer.valueOf(rs.getInt(rsidx));
            } else if (type == UUID.class) {
                value = UUID.fromString(rs.getString(rsidx));
            } else {
                value = rs.getObject(rsidx);
            }
            return value;
        }

        private void fetch() throws DAOException {
            Row next = null;
            try {
                if (rs.next()) {
                    final int cols = rs.getMetaData().getColumnCount();
                    Object[] data = new Object[cols];
                    for (int i = 0; i < cols; i++) {
                        data[i] = getValue(rs, i);
                    }
                    next = Rows.row(data, index);
                }
            } catch (SQLException sqle) {
                throw new DAOException("Error fetching next row", sqle);
            }
            row = next;
        }

        public void close() {
            try {
                RowReader.close(rs);
            } catch (SQLException ex) {
                Logger.getLogger(RowReader.class.getName()).log(Level.SEVERE, "Error closing exception", ex);
            }
        }

        public boolean hasNext() throws DAOException {
            if (row == null) {
                fetch();
            }
            return row != null;
        }
    }
}
