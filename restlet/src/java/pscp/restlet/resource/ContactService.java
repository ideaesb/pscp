package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Contacts;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import pscp.restlet.URLS;
import pscp.restlet.template.ResourceTemplate;
import pscp.restlet.template.RowTransformer;
import pscp.restlet.template.TableTemplate;

/**
 *
 * @author iws
 */
public class ContactService extends ServiceResource {

    static {
        declareDAODependency(ContactService.class, Contacts.class);
    }

    @Override
    protected ResourceTemplate buildTemplate(RowIterator rows) {
        TableTemplate table = new TableTemplate(rows, tableColumns(rows), getRowTransformer(rows));
        return table;
    }

    protected boolean isModified() {
        return true;
    }

    @Override
    protected List<Column> tableColumns(RowIterator rows) {
        List<Column> cols = new ArrayList<Column>(rows.columns());
        cols.remove(Contacts.CONTACTID);
        cols.remove(Contacts.PASSWORD);
        cols.add(new Column("active","Account Active",Boolean.class));
        return cols;
    }

    @Override
    protected RowTransformer getRowTransformer(RowIterator rows) {
        final URLS urls = urls();
        Row currentUser = currentUser();
        final boolean admin = currentUser == Row.NULL ? false : currentUser.value(Contacts.ADMIN);
        final String currentUserID = currentUser.string(Contacts.CONTACTID);
        return new RowTransformer() {

            public TemplateModel row(Row row) {
                SimpleSequence seq = new SimpleSequence();
                String userID = row.string(Contacts.CONTACTID);
                if (admin || userID.equals(currentUserID)) {
                    String editURL = urls.getURL(URLS.Name.CONTACT_FORM, "id", userID);
                    seq.add(href(editURL, row.string(Contacts.PERSON)));
                } else {
                    seq.add(row.string(Contacts.PERSON));
                }
                seq.add(row.string(Contacts.PHONE));
                seq.add(row.string(Contacts.EMAIL));
                seq.add(row.string(Contacts.ADMIN));
                seq.add(row.string(Contacts.LASTLOGIN));
                boolean accountActive = row.string(Contacts.PASSWORD).length() > 0;
                seq.add(Boolean.toString(accountActive));
                return seq;
            }
        };
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        return dao(Contacts.class).readContacts();
    }

    @Override
    protected void initServiceResource() {
    }

    @Override
    protected String getServiceTitle() {
        return "Contacts";
    }
}
