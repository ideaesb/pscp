
package dao;

import java.util.List;

/**
 *
 * @author iws
 */
public interface RowIterator {

    List<Column> columns();
    boolean hasNext() throws DAOException;
    Row next() throws DAOException;
    void close();
}
