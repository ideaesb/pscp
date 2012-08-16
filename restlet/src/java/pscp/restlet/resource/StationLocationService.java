package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Form;
import org.restlet.resource.Representation;
import pscp.restlet.URLS;
import pscp.restlet.util.Representations;
import pscp.restlet.template.TemplateModels;
import pscp.restlet.template.RowTransformer;

/**
 *
 * @author iws
 */
public class StationLocationService extends BaseStationService {

    static {
        declareDAODependency(StationLocationService.class, Stations.class);
    }
    @Override
    protected String getServiceTitle() {
        return "Station Locations";
    }

    @Override
    protected TemplateModel createTemplateModel(final RowIterator rows) throws DAOException {
        SimpleHash table = new SimpleHash();
        List<Column> cols = new ArrayList<Column>(rows.columns());
        cols.add(new Column("info", "Info", String.class));
        cols.add(new Column("products", "Products", String.class));
        table.put("cols", TemplateModels.sequenceCols(cols));
        table.put("rows", TemplateModels.tableModel(rows, new RowTransformer() {

            public TemplateModel row(Row row) {
                SimpleSequence seq = TemplateModels.sequenceRow(row, rows.columns());
                String format = "<a href='%s'>%s</a>";
                seq.add(String.format(format, getInfoURL(row), "GO"));
                seq.add(String.format(format, getProductsURL(row), "GO"));
                return seq;
            }
        }));
        return table;
    }

    @Override
    protected Representation json(final RowIterator rows) throws DAOException, JSONException {
        return Representations.tableJSON(rows, new Representations.JSONCustomizer() {

            public void addAttributes(JSONObject obj, Row next) throws JSONException {
                obj.put("info", getInfoURL(next));
                obj.put("products", getProductsURL(next));
            }
        }, Stations.STATIONID);
    }
}
