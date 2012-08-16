package dao.pscp;

import dao.Column;
import dao.DAO;
import dao.DAOException;
import dao.Pair;
import dao.RowIterator;
import dao.SQLWriter;
import dao.TableDefinition;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ProductRevisions extends DAO {

    private static final TableDefinition columns = new TableDefinition("productrev");
    public static final Column<Integer> REV = columns.integer("rev");
    public static final Column<Integer> ID = columns.integer("id");
    public static final Column<String> LOCATION = columns.string("location");
    public static final Column<Date> CHANGED = columns.date("changed");
    public static final Column<Boolean> APPROVED = columns.bool("approved");
    public static final Column<String> ORIGINAL = columns.string("original");
    public static final Column<String> COMMENT = columns.string("comment");
    public static final Column<UUID> SUBMITTER = columns.uuid("submitter");
    public static final Column<String> MD5 = columns.string("md5");

    public RowIterator readAll() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public RowIterator readRevision(int product, int rev) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnsEquals(ID,REV);
        return readRows(sql,ID.pair(product),REV.pair(rev));
    }

    public RowIterator readProductRevisions(int product) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(ID);
        return readRows(sql,ID.pair(product));
    }

    public RowIterator readRevisions(List<Integer> rev) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereIn(ID,rev);
        return readRows(sql);
    }

    public void publish(int product,int rev) throws DAOException {
        updateWhereEquals(columns.table(), ID.pair(product), APPROVED.pair(Boolean.FALSE));
        updateWhereEquals(columns.table(), REV.pair(product), APPROVED.pair(Boolean.TRUE));
    }

    public Integer find(int productID,String md5) throws DAOException {
        RowIterator rows = selectAllFromWhere(columns.table(), ID.pair(productID),MD5.pair(md5));
        Integer id = null;
        if (rows.hasNext()) {
            id = rows.next().value(REV);
        }
        return id;
    }

    public int addRevision(int product,String location,String original,String comment,UUID submitter,String md5) throws DAOException {
        return addRevision(
                ID.pair(product),
                LOCATION.pair(location),
                ORIGINAL.pair(original),
                COMMENT.pair(comment),
                APPROVED.pair(Boolean.FALSE),
                SUBMITTER.pair(submitter),
                MD5.pair(md5));
    }

    public int addRevision(Pair... values) throws DAOException {
        Number id = (Number) insert(columns.table(),REV,values);
        return id.intValue();
    }
}
