package pscp.restlet.template;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import freemarker.template.SimpleCollection;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.restlet.data.Parameter;

/**
 *
 * @author iws
 */
public abstract class TemplateModels {

    public static TemplateModel singleRow(RowIterator rows) throws DAOException {
        Row row = rows.next();
        return rowModel(row, idx(rows.columns()));
    }

    private static Map<String, Column> idx(List<Column> cols) {
        Map<String, Column> idx = new HashMap<String, Column>();
        for (int i = 0; i < cols.size(); i++) {
            idx.put(cols.get(i).columnName(), cols.get(i));
        }
        return idx;
    }

    private static TemplateModel rowModel(final Row row, final Map<String, Column> idx) {
        return new TemplateHashModel() {

            public TemplateModel get(String arg0) throws TemplateModelException {
                Column col = idx.get(arg0);
                if (col == null) {
                    throw new TemplateModelException("Cannot find " + arg0);
                }
                return new SimpleScalar(row.string(col));
            }

            public boolean isEmpty() throws TemplateModelException {
                return false;
            }
        };
    }

    public static TemplateModel singleItem(final TemplateModel model) throws DAOException {
        return new TemplateHashModel() {

            public TemplateModel get(String arg0) throws TemplateModelException {
                return model;
            }

            public boolean isEmpty() throws TemplateModelException {
                return model == null;
            }
        };
    }

    public static TemplateModel createRowIteratorModel(String root, final RowIterator rows) throws DAOException {
        return singleItem(hashCollection(rows));
    }

    private static TemplateModel hashCollection(final RowIterator rows) {
        final Map<String, Column> idx = idx(rows.columns());
        return new TemplateCollectionModel() {

            public TemplateModelIterator iterator() throws TemplateModelException {
                return new TemplateModelIterator() {

                    public TemplateModel next() throws TemplateModelException {
                        final Row current;
                        try {
                            current = rows.next();
                        } catch (DAOException ex) {
                            throw new TemplateModelException("Error fetching next row", ex);
                        }
                        return rowModel(current, idx);
                    }

                    public boolean hasNext() throws TemplateModelException {
                        try {
                            return rows.hasNext();
                        } catch (DAOException ex) {
                            throw new TemplateModelException("Error checking next row", ex);
                        }
                    }
                };
            }
        };
    }

    private static TemplateModel rowCollection(final RowIterator rows, final Row.Formatter fmt,
            final List<Column> display) {
        return new TemplateCollectionModel() {

            public TemplateModelIterator iterator() throws TemplateModelException {
                return new TemplateModelIterator() {

                    public TemplateModel next() throws TemplateModelException {
                        final Row current;
                        try {
                            current = rows.next();
                        } catch (DAOException ex) {
                            throw new TemplateModelException("Error fetching next row", ex);
                        }
                        SimpleSequence row = new SimpleSequence();
                        for (Column c : display) {
                            row.add(fmt.string(current, c));
                        }
                        return row;
                    }

                    public boolean hasNext() throws TemplateModelException {
                        try {
                            return rows.hasNext();
                        } catch (DAOException ex) {
                            throw new TemplateModelException("Error checking next row", ex);
                        }
                    }
                };
            }
        };
    }

    public static TemplateModel tableModel(RowIterator rows) {
        return tableModel(rows, defaultRowTransformer(rows.columns()));
    }

    public static TemplateModel tableModel(final RowIterator rows, final RowTransformer transformer) {
        return new TemplateCollectionModel() {

            public TemplateModelIterator iterator() throws TemplateModelException {
                return new TemplateModelIterator() {

                    public TemplateModel next() throws TemplateModelException {
                        try {
                            return transformer.row(rows.next());
                        } catch (DAOException ex) {
                            throw new TemplateModelException(ex);
                        }
                    }

                    public boolean hasNext() throws TemplateModelException {
                        try {
                            return rows.hasNext();
                        } catch (DAOException ex) {
                            throw new TemplateModelException(ex);
                        }
                    }
                };
            }
        };
    }

    public static TemplateModel tableModel(RowIterator rows, Row.Formatter formatter, List<Column> hide) {
        SimpleHash hash = new SimpleHash();
        hash.put("cols", hashColumns(rows.columns(), hide));
        List<Column> display = new ArrayList(rows.columns());
        display.removeAll(hide);
        hash.put("rows", rowCollection(rows, formatter, display));
        return hash;
    }

    public static SimpleHash hashRow(Row row, List<Column> columns) {
        SimpleHash hash = new SimpleHash();
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            hash.put(column.columnName(), row.string(column));
        }
        return hash;
    }

    public static SimpleSequence sequenceRow(Row row, List<Column> cols) {
        SimpleSequence seq = new SimpleSequence();
        for (int i = 0; i < cols.size(); i++) {
            seq.add(row.string(cols.get(i)));
        }
        return seq;
    }

    public static SimpleSequence sequenceCols(List<Column> cols) {
        SimpleSequence seq = new SimpleSequence();
        for (int i = 0; i < cols.size(); i++) {
            seq.add(cols.get(i).displayName());
        }
        return seq;
    }

    public static SimpleHash createLinkModel(String url, String name, String... atts) {
        SimpleHash link = new SimpleHash();
        link.put("url", url);
        link.put("name", name);
        if (atts.length % 2 != 0) {
            throw new IllegalArgumentException("atts must be provided in pairs");
        }
        for (int i = 0; i < atts.length; i += 2) {
            link.put(atts[i], atts[i + 1]);
        }
        return link;
    }

    public static TemplateModel hashColumns(List<Column> cols) {
        List<Column> hide = Collections.emptyList();
        return hashColumns(cols, hide);
    }

    public static TemplateModel hashColumns(List<Column> cols, List<Column> hide) {
        SimpleSequence names = new SimpleSequence();
        for (Column c : cols) {
            if (!hide.contains(c)) {
                names.add(c.displayName());
            }
        }
        return names;
    }

    public static TemplateModel formModel(String url, List<Pair> atts,
            List<Pair> hide) {
        SimpleHash form = new SimpleHash();
        form.put("url", url);
        form.put("atts", hashAtts(atts));
        form.put("hidden", hashAtts(hide));
        return form;
    }

    private static SimpleSequence hashAtts(List<Pair> pairs) {
        SimpleSequence atts = new SimpleSequence();
        for (Pair p : pairs) {
            SimpleHash hash = new SimpleHash();
            hash.put("name", p.column().columnName());
            hash.put("display", p.column().displayName());
            hash.put("value", p.value() == null ? "" : p.value());
            atts.add(hash);
        }
        return atts;
    }

    public static class FormModel implements TemplateHashModelEx {

        private Map data = new HashMap();
        private SimpleHash hidden = new SimpleHash();

        public FormModel() {
            data.put("hidden", hidden);
        }

        public void setAttributes(List<Pair> pairs) {
            SimpleHash atts = new SimpleHash();
            for (int i = 0; i < pairs.size(); i++) {
                SimpleHash attData = new SimpleHash();
                Pair pair = pairs.get(i);
                attData.put("value", pair.value());
                attData.put("name", pair.column().columnName());
                attData.put("label", pair.column().displayName());
                atts.put(pair.column().columnName(), attData);
            }
            data.put("atts", atts);
        }

        public void addAttribute(String name, String label, Object value) {
            SimpleHash atts = (SimpleHash) data.get("atts");
            SimpleHash attData = new SimpleHash();
            attData.put("value", value);
            attData.put("name", name);
            attData.put("label", label);
            atts.put(name, attData);
        }

        public void setHidden(Pair... pairs) {
            setHidden(Arrays.asList(pairs));
        }

        public void setHidden(List<Pair> pairs) {
            for (int i = 0; i < pairs.size(); i++) {
                SimpleHash attData = new SimpleHash();
                Pair pair = pairs.get(i);
                attData.put("value", pair.value());
                attData.put("name", pair.column().columnName());
                hidden.put(pair.column().columnName(), attData);
            }
        }

        public void setAutocompleteURL(Column col, String url) {
            SimpleHash atts = (SimpleHash) data.get("atts");
            SimpleHash attData;
            try {
                attData = (SimpleHash) atts.get(col.columnName());
            } catch (TemplateModelException ex) {
                throw new IllegalArgumentException("no column " + col);
            }
            attData.put("acURL", url);
        }

        public void setParameters(Column col, Parameter... params) {
            SimpleHash atts = (SimpleHash) data.get("atts");
            SimpleHash attData;
            try {
                attData = (SimpleHash) atts.get(col.columnName());
            } catch (TemplateModelException ex) {
                throw new IllegalArgumentException("no column " + col);
            }
            attData.put("params", createParameters(params));
        }

        public TemplateModel get(String arg0) throws TemplateModelException {
            return (TemplateModel) data.get(arg0);
        }

        public boolean isEmpty() throws TemplateModelException {
            return false;
        }

        public void setURL(String toString) {
            data.put("url", new SimpleScalar(toString));
        }

        public int size() throws TemplateModelException {
            return data.size();
        }

        public TemplateCollectionModel keys() throws TemplateModelException {
            return new SimpleCollection(data.keySet());
        }

        public TemplateCollectionModel values() throws TemplateModelException {
            return new SimpleCollection(data.values());
        }
    }

    public static SimpleHash createField(String name, String label) {
        SimpleHash field = new SimpleHash();
        field.put("name", name);
        field.put("label", label);
        return field;
    }

    public static SimpleSequence createParameters(Parameter... params) {
        SimpleSequence seq = new SimpleSequence();
        for (int i = 0; i < params.length; i++) {
            SimpleHash param = new SimpleHash();
            param.put("name", params[i].getName());
            param.put("value", params[i].getValue());
            seq.add(param);
        }
        return seq;
    }

    public static SimpleHash createField(String name, String label, Parameter... params) {
        SimpleHash field = new SimpleHash();
        field.put("name", name);
        field.put("label", label);
        field.put("params", createParameters(params));
        return field;
    }

    public static RowTransformer defaultRowTransformer(final List<Column> cols) {
        return new RowTransformer() {

            public TemplateModel row(Row row) {
                return TemplateModels.sequenceRow(row, cols);
            }
        };
    }
}
