package pscp.restlet.util;

import dao.Column;
import dao.Columns;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 *
 * @author iws
 */
public class Representations {

    public static Representation tableJSON(RowIterator rows, Column... columnsToHide) throws DAOException, JSONException {
        return tableJSON(rows, null, columnsToHide);
    }

    public static Representation tableJSON(RowIterator rows, JSONCustomizer customizer, Column... columnsToHide) throws
            DAOException, JSONException {
        JSONArray array = new JSONArray();
        List<Column> cols = Columns.filter(rows.columns(),columnsToHide);
        try {
            while (true) {
                Row next = rows.next();
                if (next == null) {
                    break;
                }
                JSONObject obj = new JSONObject();
                for (Column col : cols) {
                    obj.put(col.columnName(), next.value(col));
                }
                if (customizer != null) {
                    customizer.addAttributes(obj, next);
                }
                array.put(obj);
            }
        } finally {
            rows.close();
        }
        return new JsonRepresentation(array);
    }

    public static Representation tableJSON(RowIterator rows) throws DAOException, JSONException {
        return tableJSON(rows, (JSONCustomizer) null);
    }

    public static interface JSONCustomizer {
        void addAttributes(JSONObject obj, Row next) throws JSONException;
    }
}
