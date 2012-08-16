
package dao;

import java.sql.Savepoint;

/**
 *
 * @author iws
 */
public interface DAOCollection {

    Savepoint savepoint() throws DAOException;

    void commit() throws DAOException;

    void rollback() throws DAOException;

    void rollback(Savepoint savepoint) throws DAOException;

    <T extends DAO> T get(Class<T> type) throws DAOException;

    void close();
}
