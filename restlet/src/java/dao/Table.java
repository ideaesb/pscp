/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package dao;

import java.util.Iterator;
import java.util.List;

/**
 *
 * @author en
 */
public interface Table extends Iterable<Row> {

    List<Column> columns();

    Row row(int i);

    int rowCount();

    Iterator<Row> iterator();

    RowIterator rowIterator();

}
