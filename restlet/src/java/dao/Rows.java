/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author en
 */
public class Rows {

    public static <K, V> Map<K, V> lookup(RowIterator rows, Column<K> key, Column<V> value) throws DAOException {
        Map<K, V> lookup = new HashMap<K, V>();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(key), r.value(value));
        }
        return lookup;
    }

    public static <K> Map<K, Row> rowLookup(RowIterator rows, Column<K> key) throws DAOException {
        Map<K, Row> lookup = new HashMap<K, Row>();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(key), r);
        }
        return lookup;
    }
    public static <K,V> Map<K, V> rowLookup(RowIterator rows, Column<K> key,Column<V> value) throws DAOException {
        Map<K, V> lookup = new HashMap<K, V>();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.value(key), r.value(value));
        }
        return lookup;
    }
    public static Map<String, Row> rowLookupString(RowIterator rows, Column key) throws DAOException {
        Map<String, Row> lookup = new HashMap<String, Row>();
        while (rows.hasNext()) {
            Row r = rows.next();
            lookup.put(r.string(key), r);
        }
        return lookup;
    }
    public static RowIterator filterOnColumn(final RowIterator rows,final Column col,final Object val) throws DAOException {
        return new RowIterator() {

            Row next;

            public List<Column> columns() {
                return rows.columns();
            }

            public boolean hasNext() throws DAOException {
                if (next == null) {
                    if (rows.hasNext()) {
                        next = findNext();
                    }
                }
                return next != null;
            }

            public Row next() throws DAOException {
                if (next == null) next = findNext();
                Row n = next;
                next = null;
                return n;
            }

            public void close() {
                rows.close();
            }

            private Row findNext() throws DAOException {
                Row n = null;
                while (rows.hasNext()) {
                    Row r = rows.next();
                    if (val.equals(r.value(col))) {
                        n = r;
                        break;
                    }
                }
                return next;
            }
        };
    }

    static class BaseArrayRow implements Row {

        private final Object[] data;
        private final Map<Column,Integer> idx;

        BaseArrayRow(Object[] data,Map<Column,Integer> idx) {
            this.data = data;
            this.idx = idx;
        }

        protected int index(Column col) {
            Integer i = idx.get(col);
            return i == null ? -1 : i.intValue();
        }

        public String string(Column col) {
            int idx = index(col);
            if (idx < 0) throw new IllegalArgumentException(col + " not found " + this);
            return data[idx] == null ? "" : data[idx].toString();
        }

        public <T> T value(Column<T> col) {
            Object val = data[index(col)];
            try {
            return (T) val == null ? null : col.type().cast(val);
            } catch (ClassCastException cce) {
                throw new ClassCastException("Expected " + col.type().getName() + " found a " + val.getClass().getName());
            }
        }
        
        public String toString() {
            StringBuilder sb = new StringBuilder('[');
            for (int i = 0; i < data.length; i++) {
                sb.append(data[i]);
                if (i + 1 < data.length) {
                    sb.append(',');
                }
            }
            return sb.append(']').toString();
        }

    }

    public static Row mapRow(final Map<Column,Object> data) {
        return new Row() {

            public String string(Column col) {
                Object val = data.get(col);
                return val == null ? "" : val.toString();
            }

            public <T> T value(Column<T> col) {
                Object val = data.get(col);
                return val == null ? null : col.type().cast(val);
            }

            public String toString() {
                return "mapRow data:" + data;
            }
        };
    }

    public static Row row(Object[] data, final Map<Column,Integer> index) {
        return new BaseArrayRow(data,index);
    }
}
