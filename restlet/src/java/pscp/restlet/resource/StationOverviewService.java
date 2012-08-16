package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.TableDefinition;
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
public class StationOverviewService extends BaseStationService {

    static {
        declareDAODependency(StationOverviewService.class, Stations.class);
    }
    @Override
    protected String getServiceTitle() {
        return "Station Overview";
    }

    @Override
    protected TemplateModel createTemplateModel(final RowIterator rows) throws DAOException {
        SimpleHash table = new SimpleHash();
        final List<Column> cols = new ArrayList<Column>();
        cols.add(Stations.STATIONNAME);
        cols.add(Stations.STATIONCLASS);
        cols.add(Stations.REGION);
        cols.add(Stations.NATION);
        cols.add(Stations.QUALITY);
        cols.add(Stations.CNT);
        table.put("cols", TemplateModels.sequenceCols(cols));
        table.put("rows", TemplateModels.tableModel(rows, new RowTransformer() {

            public TemplateModel row(Row row) {
                SimpleSequence seq = new SimpleSequence();
                seq.add(href(getPageURL(row),row.string(Stations.STATIONNAME)));
                seq.add(row.string(Stations.STATIONCLASS));
                seq.add(row.string(Stations.REGION));
                seq.add(row.string(Stations.NATION));
                seq.add(row.string(Stations.QUALITY));
                seq.add(row.string(Stations.CNT));
                return seq;
            }
        }));
        return table;
    }

    @Override
    protected RowIterator resolveStationsTable(String process, String region, Integer quality, String[] groupid, String[] indicator) throws DAOException {
        return dao(Stations.class).readStations(process, region);
    }




}
