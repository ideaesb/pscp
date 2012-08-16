package dao;

import java.util.List;

/**
 */
public class SQLWriter {

    private final StringBuilder buf;
    private static final char TAB = '\t';
    private static final char NL = '\n';
    private final char lquote;
    private final char rquote;

    public SQLWriter(char[] quotes) {
        lquote = quotes[0];
        rquote = quotes[1];
        buf = new StringBuilder();
    }

    protected SQLWriter quote(String s) {
        buf.append(lquote).append(s).append(rquote);
        return this;
    }

    protected SQLWriter nl() {
        buf.append('\n');
        return this;
    }

    protected SQLWriter append(String string) {
        buf.append(string);
        buf.append(NL);
        return this;
    }

    protected void commaNLTAB() {
        buf.append(',').append('\n').append('\t');
    }

    protected void appendWithTAB(String string) {
        buf.append(TAB);
        append(string);
    }

    public String toString() {
        return buf.toString();
    }

    protected void appendWithSchema(String string, String schema) {
        buf.append(TAB);
        maybeAppendSchema(schema);
        append(string);
    }

    protected void appendAs(String Table, String column, String as, String schema) {
        maybeAddSpace();
        buf.append(TAB);
        addTableColumn(Table, column, schema);
        buf.append(' ').append("AS").append(' ');
        buf.append(as);
        buf.append(',').append('\n');
    }

    /**
     * Build a simple inner join like so:
     * INNER JOIN schema.Table ON schema.leftTable.leftColumn = rightTable.rightColumn
     */
    protected void appendInnerJoin(String Table, String leftTable, String leftColumn, String rightTable,
            String rightColumn, String schema) {
        maybeAddSpace();
        buf.append("INNER JOIN").append('\n');
        buf.append(TAB);
        addWithSchema(Table, schema);
        addWord("ON");
        addTableColumn(leftTable, leftColumn, schema);
        addWord('=');
        addTableColumn(rightTable, rightColumn, schema);
    }

    protected void maybeAddSpace() {
        if (!Character.isWhitespace(buf.charAt(buf.length() - 1))) {
            buf.append(' ');
        }
    }

    protected void addWord(String word) {
        buf.append(' ').append(word).append(' ');
    }

    protected void addWord(char word) {
        buf.append(' ').append(word).append(' ');
    }

    protected void addTableColumn(String Table, String column, String schema) {
        maybeAppendSchema(schema);
        buf.append(Table).append('.').append(column);
    }

    protected void addWithSchema(String string, String schema) {
        maybeAppendSchema(schema);
        buf.append(string);
    }

    private void maybeAppendSchema(String schema) {
        if (schema != null && !schema.equals("")) {
            buf.append(schema + ".");
        }
    }

    protected void trimLastComma() {
        for (int index = buf.length() - 1; index > -1; index--) {
            if (buf.charAt(index) == ',') {
                buf.deleteCharAt(index);
                return;
            }
        }
    }

    protected SQLWriter endLine(String word) {
        buf.append(word).append(NL);
        return this;
    }

    protected SQLWriter insertInto(String table) {
        buf.append("INSERT INTO ");
        endLine(table);
        return this;
    }

    protected SQLWriter sequence(Column[] cols) {
        buf.append(cols[0].columnName());
        for (int i = 1; i < cols.length; i++) {
            buf.append(',');
            buf.append(cols[i].columnName());
        }
        return this;

    }

    protected SQLWriter quotedSequence(Column[] cols) {
        buf.append(cols[0].columnName());
        for (int i = 1; i < cols.length; i++) {
            buf.append(',');
            quote(cols[i].columnName());
        }
        return this;
    }

    protected SQLWriter quotedSequence(Pair[] cols) {
        buf.append(cols[0].column().columnName());
        for (int i = 1; i < cols.length; i++) {
            buf.append(',');
            quote(cols[i].column().columnName());
        }
        return this;
    }

    protected SQLWriter valuesHolder(Pair[] pairs) {
        addWord("VALUES");
        buf.append('(');
        buf.append(pairs[0].useDefaultValue() ? "DEFAULT" : "?");
        for (int i = 1; i < pairs.length; i++) {
            buf.append(',');
            buf.append(pairs[i].useDefaultValue() ? "DEFAULT" : "?");
        }
        buf.append(')');
        return this;
    }

    protected SQLWriter columnNames(Column[] cols) {
        buf.append('(');
        quotedSequence(cols);
        buf.append(')');
        return this;
    }

    protected SQLWriter columnNames(Pair[] values) {
        buf.append('(');
        quotedSequence(values);
        buf.append(')');
        return this;
    }

    protected SQLWriter selectFrom(String table, Column[] cols) {
        addWord("SELECT");
        quotedSequence(cols);
        addWord("FROM");
        addWord(table);
        return this;
    }

    public SQLWriter whereIn(Column col,List vals) {
        addWord("WHERE");
        quote(col.columnName());
        addWord("IN");
        buf.append('(');
        buf.append(vals.get(0));
        for (int i = 1; i < vals.size(); i++) {
            buf.append(',');
            buf.append('\'');
            buf.append(vals.get(i));
            buf.append('\'');
        }
        buf.append(')');
        return this;
    }

    public SQLWriter whereColumnEquals(Column col) {
        addWord("WHERE");
        quote(col.columnName());
        addWord("=");
        addWord("?");
        return this;
    }

    public SQLWriter whereColumnsEquals(Column... cols) {
        if (cols.length > 0 && allNotNull(cols)) {
            addWord("WHERE");
            boolean wroteOne = false;
            for (int i = 0; i < cols.length; i++) {
                if (cols[i] == null) continue;
                if (wroteOne) addWord("AND");
                wroteOne = true;
                quote(cols[i].columnName());
                addWord("=");
                addWord("?");
            }
        }
        return this;
    }

    private boolean allNotNull(Object[] args) {
        boolean nonNull = false;
        for (int i = 0; i < args.length && !nonNull; i++) {
            nonNull |= args[i] != null;
        }
        return nonNull;
    }

    public SQLWriter wherePairsEquals(List<Pair> pairs) {
        if (pairs.size() > 0) {
            addWord("WHERE");
            for (int i = 0; i < pairs.size(); i++) {
                if (i > 0) addWord("AND");
                quote(pairs.get(i).column().columnName());
                addWord("=");
                addWord("?");
            }
        }
        return this;
    }

    // this version does not allow for multiple order by columns!
    public SQLWriter orderBy(Column col) {
        // @todo ASC/DEC
        addWord("ORDER BY");
        quote(col.columnName());
        return this;
    }
    
    public SQLWriter orderBy(String colnames) {
        addWord("ORDER BY " + colnames);
        return this;
    }

    public SQLWriter update(String table, Pair[] values) {
        addWord("UPDATE");
        addWord(table);
        addWord("SET");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                addWord(',');
            }
            addWord(values[i].column().columnName());
            addWord('=');
            if (values[i].useDefaultValue()) {
                addWord("DEFAULT");
            } else {
                addWord('?');
            }
        }
        return this;
    }
}
