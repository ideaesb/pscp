
package dao;

/**
 *
 * @author iws
 */
public interface Query {

    long life();

    RowIterator results() throws DAOException;

    void clear();
}
