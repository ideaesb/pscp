package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iws
 */
public abstract class DAO {

    private Connection connection;
    private Logger logger;
    private static final Object queryLock = new Object();
    private TableDefinition tableDefintion;

    void setConnection(Connection conn) {
        this.connection = conn;
    }

    void setTableDefinition(TableDefinition def) {
        this.tableDefintion = def;
    }

    void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected void tableUpdated() {

    }

    protected String getTableName() {
        return tableDefintion.table();
    }

    protected List<Column> getColumns() {
        return tableDefintion.cook();
    }

    protected SQLWriter createWriter() {
        return new SQLWriter(new char[] {' ',' '});
    }

    protected Logger logger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    protected Object insert(String table,Column returning,Pair... values) throws DAOException {
        PreparedStatement ps = null;
        SQLWriter sql = insertStatement(table, values);
        if (returning != null) {
            sql.addWord(" RETURNING " + returning.columnName());
        }
        try {
            ps = connection.prepareStatement(sql.toString());
            int idx = 1;
            for (int i = 0; i < values.length; i++) {
                if (!values[i].useDefaultValue()) {
                    ps.setObject(idx++,values[i].value(),values[i].column().sqlType());
                }
            }
            ps.execute();
            ResultSet rs = ps.getResultSet();
            Object result = null;
            if (rs != null && rs.next()) {
                result = rs.getObject(1);
            }
            return result;
        } catch (SQLException sqle) {
            throw new DAOException("Error during insert : " + sql + " with args " + Arrays.asList(values), sqle);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                logger().log(Level.SEVERE, null, ex);
            }
        }
    }

    protected Object insert(String table, Pair... values) throws DAOException {
        return insert(table,null,values);
    }

    protected int executeDelete(SQLWriter buf,List<Pair> where) throws DAOException {
        String sql = buf.toString();
        PreparedStatement ps = null;
        int changed = 0;
        try {
            ps = connection.prepareStatement(sql);
            int idx = 1;
            for (int i = 0; i < where.size(); i++) {
                Pair p = where.get(i);
                if (!p.useDefaultValue()) {
                    ps.setObject(idx++,p.value(),p.column().sqlType());
                }
            }
            changed = ps.executeUpdate();
        } catch (SQLException sqle) {
            throw new DAOException("Error during delete : " + sql + " with args " + Arrays.asList(where), sqle);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                logger().log(Level.SEVERE, null, ex);
            }
        }
        return changed;
    }

    protected void updateWhereEquals(String table,Pair where,Pair... values) throws DAOException {
        SQLWriter buf = createWriter();
        buf.update(table,values);
        buf.whereColumnEquals(where.column());
        String sql = buf.toString();
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement(sql);
            int idx = 1;
            for (int i = 0; i < values.length; i++) {
                if (!values[i].useDefaultValue()) {
                    ps.setObject(idx++,values[i].value(),values[i].column().sqlType());
                }
            }
            ps.setObject(idx++, where.value(),where.column().sqlType());
            ps.executeUpdate();
        } catch (SQLException sqle) {
            throw new DAOException("Error during insert : " + sql + " with args " + Arrays.asList(values), sqle);
        } finally {
            try {
                ps.close();
            } catch (SQLException ex) {
                logger().log(Level.SEVERE, null, ex);
            }
        }
    }

    protected SQLWriter insertStatement(String table, Pair... values) throws DAOException {
        SQLWriter buf = createWriter();
        buf.insertInto(table);
        buf.columnNames(values);
        buf.valuesHolder(values);
        return buf;
    }

    protected SQLWriter selectColumnsFrom(String table, Column<?>... cols) {
        SQLWriter sb = createWriter();
        return sb.selectFrom(table, cols);
    }

    protected RowIterator selectAllFromWhere(String table,Pair... values) throws DAOException {
        return selectAllFromWhere(table, Arrays.asList(values));
    }

    protected RowIterator selectAllFromWhere(String table,List<Pair> values) throws DAOException {
        SQLWriter buf = selectAllFrom(table);
        buf.wherePairsEquals(values);
        try {
            return RowReader.readRows(getClass(), read(buf, values));
        } catch (SQLException ex) {
            throw new DAOException("Error reading",ex);
        }
    }
    protected SQLWriter selectAllFrom(TableDefinition table) {
        return selectAllFrom(table.table());
    }

    protected SQLWriter selectAllFrom(String table) {
        return createWriter().append("SELECT * from ").append(table);
    }

    protected SQLWriter selectDistintFrom(TableDefinition def,Column... cols) {
        SQLWriter sql = createWriter().append("SELECT DISTINCT");
        if (cols.length == 0) {
            sql.addWord("*");
        } else {
            sql.columnNames(cols);
        }
        sql.addWord("FROM");
        sql.addWord(def.table());
        return sql;
    }

    protected SQLWriter deleteFromWhere(TableDefinition def,Pair... pairs) {
        return deleteFromWhere(def,Arrays.asList(pairs));
    }
    protected SQLWriter deleteFromWhere(TableDefinition def,List<Pair> pairs) {
        SQLWriter sql = createWriter().append("DELETE FROM " + def.table());
        sql.wherePairsEquals(pairs);
        return sql;
    }

    // this allows arbitrary complex queries to be created, but marks the code
    // as fishy
    protected SQLWriter appendComplexQuery(SQLWriter sql,String part) {
        return sql.append(part);
    }

    private final ResultSet read(String sql) throws DAOException {
        try {
            return connection.createStatement().executeQuery(sql);
        } catch (SQLException ex) {
            throw new DAOException("Error reading " + sql, ex);
        }
    }
    private final ResultSet read(SQLWriter sql, List<Pair> args) throws DAOException {
        try {
            PreparedStatement ps = connection.prepareStatement(sql.toString());
            for (int i = 0; i < args.size(); i++) {
                Pair arg = args.get(i);
                ps.setObject(i + 1, arg.value(),arg.column().sqlType());
            }
            return ps.executeQuery();
        } catch (SQLException ex) {
            throw new DAOException("Error reading " + sql + " using args " + args, ex);
        }
    }

    private final ResultSet read(String sql, Pair... args) throws DAOException {
        PreparedStatement ps = null;
        int tc = 0;
        int ac = 0;
        String types = "";
        try {
            ps = connection.prepareStatement(sql);
            int cnt = 1;
            for (int i = 0; i < args.length; i++) {
		tc++;
                if (args[i] == null) continue;
                ac++;
                types += " " + args[i].column().sqlType();
                ps.setObject(cnt++, args[i].value(), args[i].column().sqlType());
            }
        } 
        catch (Exception e) {
            throw new DAOException("ResultSet.read() error substituting " + sql + " using args " + Arrays.toString(args) + " tried: " + tc + " actual: "+ac+" types: "+types, e);
	}
	try {
            return ps.executeQuery();
        } catch (SQLException ex) {
            throw new DAOException("ResultSet.read() error reading " + sql + " using args " + Arrays.toString(args) + " tried: " + tc + " actual: "+ac+" types: "+types+" "+ex.toString(), ex);
        }
    }

    protected final RowIterator readRows(SQLWriter sql) throws DAOException {
        try {
            return RowReader.readRows(getClass(), read(sql.toString()));
        } catch (SQLException ex) {
            throw new DAOException("Error executing query : " + sql,ex);
        }
    }

    protected final Row readSingle(SQLWriter sql, Pair... args) throws DAOException {
        RowIterator rows = readRows(sql, args);
        Row row;
        try {
            row = rows.next();
        } finally {
            rows.close();
        }
        return row;
    }

    protected final RowIterator readRows(String sql) throws DAOException {
		try {
		    return RowReader.readRows(getClass(), read(sql));
		} catch (SQLException ex) {
		    throw new DAOException("Error executing query : " + sql);
        }
	}

    protected final RowIterator readRowsHardCoded(String sql, Pair... args) throws DAOException {
        try {
            return RowReader.readRows(getClass(), read(sql, args));
        } catch (SQLException ex) {
            throw new DAOException("Error executing query : " + sql + " with args " + Arrays.asList(args));
        }
    }

    protected final RowIterator readRows(SQLWriter sql, Pair... args) throws DAOException {
        try {
            return RowReader.readRows(getClass(), read(sql.toString(), args));
        } catch (SQLException ex) {
            throw new DAOException("Error executing query : " + sql + " with args " + Arrays.asList(args));
        }
    }

    protected Query selectAll(final TableDefinition def) {
        return new DAOQuery() {

            @Override
            protected RowIterator execute() throws DAOException {
                return readRows(selectAllFrom(def.table()));
            }
        };
    }

    private abstract class DAOQuery implements Query {

        private Table rows;

        DAOQuery() {
            QueryCache.addQuery(this);
        }

        public RowIterator results() throws DAOException {
            synchronized (queryLock) {
                if (rows == null) {
                    rows = Tables.table(execute());
                }
                return rows.rowIterator();
            }
        }

        protected abstract RowIterator execute() throws DAOException;

        public void clear() {
            synchronized (queryLock) {
                rows = null;
            }
        }

        public long life() {
            return -1;
        }


    }
}
