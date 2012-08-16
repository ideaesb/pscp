package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;

/**
 *
 * @author iws
 */
public class DAOFactory {

    private final DataSource dataSource;
    private final Map<Class<? extends DAO>, Class<? extends DAO>> classes;

    private DAOFactory(DataSource dataSource, Map<Class<? extends DAO>, Class<? extends DAO>> classes) {
        this.dataSource = dataSource;
        this.classes = classes;
    }

    public DAOCollection create() throws DAOException {
        try {
            return new AutoCommitDAOCollection(dataSource.getConnection());
        } catch (SQLException ex) {
            throw new DAOException("Error opening connection", ex);
        }
    }

    public DAOCollection create(boolean startTransaction) throws DAOException {
        DAOCollection created;
        if (startTransaction) {
            try {
                Connection conn = dataSource.getConnection();
                conn.setAutoCommit(false);
                created = new TransactionDAOCollection(conn);
            } catch (SQLException ex) {
                throw new DAOException("Error opening connection", ex);
            }
        } else {
            created = create();
        }
        return created;
    }

    abstract class AbstractDAOCollection implements DAOCollection {

        protected Connection connection;
        private final Map<Class<? extends DAO>, DAO> daos;

        public AbstractDAOCollection(Connection connection) {
            this.connection = connection;
            daos = new HashMap<Class<? extends DAO>, DAO>();
        }

        public void rollback(Savepoint savepoint) throws DAOException {
            try {
                connection.rollback(savepoint);
            } catch (SQLException ex) {
                throw new DAOException("Error setting savepoint",ex);
            }
        }

        public Savepoint savepoint() throws DAOException {
            try {
                return connection.setSavepoint();
            } catch (SQLException ex) {
                throw new DAOException("Error setting savepoint",ex);
            }
        }

        @Override
        protected void finalize() throws Throwable {
            if (connection != null) {
                System.err.println("Connection not closed!");
            }
            close();
            super.finalize();
        }

        public final void close() {
            if (connection != null) {
                closeCollection();
                daos.clear();
                try {
                    connection.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DAOFactory.class.getName()).log(Level.SEVERE, "Error closing connection", ex);
                } finally {
                    connection = null;
                }
            }
        }

        public final <T extends DAO> T get(Class<T> type) throws DAOException {
            T t = (T) daos.get(type);
            if (t == null) {
                t = create(type,connection);
                daos.put(type, t);
            }
            return t;
        }

        protected void closeCollection() {
        }
    }

    class AutoCommitDAOCollection extends AbstractDAOCollection {

        public AutoCommitDAOCollection(Connection connection) {
            super(connection);
        }

        public void commit() {
            throw new UnsupportedOperationException("Cannot commit auto commit");
        }

        public void rollback() {
            throw new UnsupportedOperationException("Cannot rollback auto commit");
        }
    }

    class TransactionDAOCollection extends AbstractDAOCollection {

        public TransactionDAOCollection(Connection connection) {
            super(connection);
        }

        public void commit() throws DAOException {
            if (connection != null) {
                try {
                    connection.commit();
                } catch (SQLException ex) {
                    throw new DAOException("Error committing transaction", ex);
                } finally {
                    close();
                }
            }
        }

        public void rollback() throws DAOException {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    throw new DAOException("Error committing transaction", ex);
                } finally {
                    close();
                }
            }
        }
    }

    private <T extends DAO> T create(Class<T> type, Connection conn) throws DAOException {
        Class<? extends DAO> impl = classes.get(type);
        if (impl == null) {
            throw new RuntimeException("Cannot find impl for " + type.getName());
        }
        DAO dao = null;
        try {
            dao = impl.newInstance();
        } catch (Exception ex) {
            throw new Error("Error creating DAO impl " + impl.getName(), ex);
        }
        dao.setConnection(conn);
        return (T) dao;
    }

    public static Map<Class<? extends DAO>, Class<? extends DAO>> createConfigMap() {
        return new HashMap<Class<? extends DAO>, Class<? extends DAO>>();
    }

    public static DAOFactory create(DataSource dataSource,
            Map<Class<? extends DAO>, Class<? extends DAO>> classes) {
        checkClasses(classes);
        return new DAOFactory(dataSource, classes);
    }

    private static final void checkClasses(Map<Class<? extends DAO>, Class<? extends DAO>> classes) {
        // @todo ensure sanity with intf/impl
        // @todo ensure impls have public no-arg constructor
    }
}
