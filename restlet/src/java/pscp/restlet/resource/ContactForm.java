
package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Contacts;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.util.Passwords;
import pscp.restlet.util.RestletDAOUtils;

/**
 *
 * @author iws
 */
public class ContactForm extends FormResource {

    @Override
    protected String getFormTemplate() {
        return "contact-edit-form.html";
    }

    @Override
    protected void initFormResource() {
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        String id = (String) getRequest().getAttributes().get("id");
        RowIterator rows = null;
        if (id != null) {
            rows = dao(Contacts.class).getContact(UUID.fromString(id));
        }
        return rows;
    }

    @Override
    protected String objectName() {
        return "Contact";
    }

    @Override
    protected Column[] formAtts() {
        return new Column[]{
                    Contacts.PERSON,
                    Contacts.PHONE,
                    Contacts.EMAIL,
                    Contacts.ADMIN};
    }
    @Override
    protected void completeFormModel(TemplateModels.FormModel form,Row editing) throws DAOException {
        super.completeFormModel(form,editing);
        form.addAttribute("resetPassword", "Reset Password", "false");
        form.addAttribute("deactivate", "Deactivate Account", "false");
    }

    @Override
    protected Column[] formHidden() {
        return new Column[]{Contacts.CONTACTID};
    }

    private boolean checkUser(String editUser) {
        Row currentUser = currentUser();
        final boolean admin = currentUser == Row.NULL ? false : currentUser.value(Contacts.ADMIN);
        final String currentUserID = currentUser.string(Contacts.CONTACTID);
        boolean allowed = true;
        if (!admin && !currentUserID.equals(editUser)) {
            getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            allowed = false;
        }
        return allowed;
    }

    @Override
    protected void accept(Representation entity) throws DAOException {
        Form form = new Form(entity);

        String contactid = form.getFirstValue(Contacts.CONTACTID.columnName());
        if (!checkUser(contactid)) {
            return;
        }
        Contacts dao = dao(Contacts.class);
        UUID uid = null;
        if (contactid == null) {
            contactid = (String) getRequest().getAttributes().get("id");
        }
        String person = form.getFirstValue(Contacts.PERSON.columnName());
        boolean resetPassword = "on".equals(form.getFirstValue("resetPassword","off"));
        boolean deactivate = "on".equals(form.getFirstValue("deactivate","off"));
        Status status = Status.SUCCESS_CREATED;
        List<Pair> pairs = RestletDAOUtils.parseForm(form, Contacts.ADMIN, Contacts.PERSON, Contacts.PHONE, Contacts.EMAIL);
        String passwordMessage = null;
        if (resetPassword && !deactivate) {
            String newPassword = randomPassword();
            passwordMessage = "New password is : " + newPassword;
            try {
                pairs.add(Contacts.PASSWORD.pair(Passwords.encode(newPassword)));
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE,"Error encoding password '" + newPassword + "'",ex);
                getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                return;
            }
        }
        if (contactid != null) {
            uid = UUID.fromString(contactid);
            status = Status.SUCCESS_ACCEPTED;
            dao.update(uid,pairs);
        } else {
            if (person == null || person.trim().length() == 0) {
                getLogger().warning("ContactForm POST with no person name, ignoring");
                getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                return;
            }
            uid = dao.insert(pairs);
        }
        if (deactivate) {
            dao.deactivate(uid);
        }
        daoModified(Contacts.class);
        getResponse().setStatus(status);
        String textResponse = "Success";
        if (passwordMessage != null) {
            textResponse += ", " + passwordMessage;
        }
        getResponse().setEntity(textResponse + "\n", MediaType.TEXT_HTML);
    }
    
    private String randomPassword() {
        Random r = new Random();
        int nonAlphaNum = r.nextInt(7);
        char[] password = new char[7];
        char[] nonAlphaNums = new char[]{'!', '#', '$', '*'};
        for (int i = 0; i < 7; i++) {
            char ch;
            if (nonAlphaNum == i) {
                ch = nonAlphaNums[r.nextInt(nonAlphaNums.length)];
            } else {
                double n = r.nextDouble();
                if (n > .666) {
                    ch = (char) (65 + r.nextInt(26));
                } else if (n > .333) {
                    ch = (char) (97 + r.nextInt(26));
                } else {
                    ch = (char) (48 + r.nextInt(10));
                }
            }
            password[i] = ch;
        }
        return new String(password);
    }



}
