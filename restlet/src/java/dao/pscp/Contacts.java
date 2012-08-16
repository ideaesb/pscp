
package dao.pscp;

import dao.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
/**
 *
 * @author iws
 */
public class Contacts extends DAO {

    private static final TableDefinition columns = new TableDefinition("contacts");
    public static final Column<UUID> CONTACTID = columns.uuid("contactid", "ID");
    public static final Column<String> PERSON = columns.string("person", "Name");
    public static final Column<String> PHONE = columns.string("phone", "Phone Number");
    public static final Column<String> EMAIL = columns.string("email", "Email");
    public static final Column<String> PASSWORD = columns.string("password");
    public static final Column<Boolean> ADMIN = columns.bool("admin");
    public static final Column<Date> LASTLOGIN = columns.date("lastlogin");

    public void login(UUID id) throws DAOException {
        updateWhereEquals(columns.table(), CONTACTID.pair(id), LASTLOGIN.pair(new Date()));
    }

    public RowIterator readContacts() throws DAOException {
        return readRows(selectAllFrom(columns));
    }

    public RowIterator getContact(UUID id) throws DAOException {
        SQLWriter sql = selectAllFrom(columns).whereColumnEquals(CONTACTID);
        return readRows(sql,CONTACTID.pair(id));
    }

    public boolean deactivate(UUID id) throws DAOException {
        boolean deleted = true;
        try {
            Pair pair = CONTACTID.pair(id);
            List<Pair> pairs = Collections.singletonList(pair);
            SQLWriter sql = deleteFromWhere(columns, pairs);
            executeDelete(sql, pairs);
        } catch (DAOException daoe) {
            deleted = false;
            if (daoe.getCause() instanceof SQLException) {
                // @HACK - Postgres doesn't provide error codes at writing???
                SQLException sqlex = (SQLException) daoe.getCause();
                if (sqlex.getMessage().indexOf("violates foreign key constraint") >= 0) {
                    logger().log(Level.INFO,"Not an error : Handling foreign key constraint exception on deactivate contact",sqlex);
                } else {
                    throw daoe;
                }
            }
        }
        // couldn't delete, just set password to null
        if (!deleted) {
            Pair nullpass = PASSWORD.pairNull();
            update(id,Collections.singletonList(nullpass));
        }
        return deleted;
    }

    public void update(UUID id,List<Pair> pairs) throws DAOException {
        updateWhereEquals(columns.table(),CONTACTID.pair(id),
                pairs.toArray(new Pair[pairs.size()]));
    }

    public UUID insert(List<Pair> pairs) throws DAOException {
        UUID newid = UUID.randomUUID();
        ArrayList<Pair> all = new ArrayList<Pair>(pairs);
        all.add(CONTACTID.pair(newid));
        insert(columns.table(),all.toArray(new Pair[all.size()]));
        return newid;
    }

}
