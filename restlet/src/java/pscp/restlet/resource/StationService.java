package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.DataSets;
import dao.pscp.Stations;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import pscp.restlet.URLS;
import pscp.restlet.URLS.Name;
import pscp.restlet.template.RowTransformer;
import pscp.restlet.util.Hardcoded;

/**
 *
 * @author iws
 */
public class StationService extends ServiceResource {
    static {
        declareDAODependency(StationService.class, Stations.class);
    }

    private String id;

    @Override
    protected void initServiceResource() {
        id = (String) getRequest().getAttributes().get("id");
    }

    @Override
    protected String getServiceTitle() {
        return "Stations";
    }

    @Override
    protected TemplateModel createTemplateModel(RowIterator rows) throws DAOException {
        TemplateModel model;
        if (id != null) {
            model = hash(rows);
        } else {
            model = super.createTemplateModel(rows);
        }
        return model;
    }

    @Override
    protected List<Column> tableColumns(RowIterator rows) {
        List<Column> cols = new ArrayList(rows.columns());
        cols.remove(Stations.STATIONID);
        return cols;
    }



    @Override
    protected RowTransformer getRowTransformer(RowIterator rows) {
        RowTransformer transformer;
        if (id != null) {
            throw new RuntimeException("shouldn't get here");
        } else {
            final URLS urls = urls();
            transformer = new RowTransformer() {
                public TemplateModel row(Row row) {
                    SimpleSequence seq = new SimpleSequence();
                    String stationID = row.string(Stations.STATIONID);
                    String stationName = row.string(Stations.STATIONNAME);
                    seq.add(href(urls.getURL(URLS.Name.STATION_FORM,"id",stationID),stationName ));
                    seq.add(row.string(Stations.STATIONTYPE));
                    seq.add(row.string(Stations.LATITUDE));
                    seq.add(row.string(Stations.LONGITUDE));
                    seq.add(row.string(Stations.REGION));
                    seq.add(row.string(Stations.NATION));
                    seq.add(row.string(Stations.STATUS));
                    seq.add(row.string(Stations.METADATA));
                    seq.add(row.string(Stations.STATIONID));
                    seq.add(row.string(Stations.ONUM));
                    seq.add(row.string(Stations.PROCESS));
                    seq.add(row.string(Stations.ELEVATION));
                    seq.add(row.string(Stations.DATASETID));
                    seq.add(row.string(Stations.STATIONCLASS));
                    seq.add(row.string(Stations.CNT));
                    return seq;
                }
            };
        }
        return transformer;
    }

    @Override
    protected String getServiceTemplate() {
        String template;
        if (id != null) {
            template = "station-table.html";
        } else {
            template = super.getServiceTemplate();
        }
        return template;
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        Stations dao = dao(Stations.class);
        RowIterator rows = null;
        if (id != null) {
            rows = dao.readStation(Integer.parseInt(id));
        } else {
            rows = dao.readStations(null, null);
        }
        return rows;
    }

    private SimpleHash hash(RowIterator selected) throws DAOException {
        SimpleHash hash = new SimpleHash();
        Row row = selected.next();

        Integer dataSetId = row.value( Stations.DATASETID );

        String datasetQuality = "?";
        String datasetSource = "?";
        String datasetSourceID = "?";
        String datasetMetadata = "?";
        if (dataSetId != null) {
            Row dataSet = dao(DataSets.class).read(dataSetId);
            datasetQuality = dataSet.string(DataSets.QUALITY);
            datasetSource = dataSet.string(DataSets.SOURCE);
            datasetSourceID = dataSet.string(DataSets.SOURCEID);
            datasetMetadata = dataSet.string(DataSets.METADATAURL);
        }
        String stationURL = urls().getURL(Name.STATION_PAGE, "id",row.string(Stations.STATIONID));
        hash.put("stationpage",stationURL);
        hash.put(Stations.STATIONNAME.columnName(),row.string(Stations.STATIONNAME));
        hash.put(Stations.STATIONTYPE.columnName(), Hardcoded.getStationTypeDisplayName(row.string(Stations.STATIONTYPE)));
        hash.put(Stations.STATIONCLASS.columnName(), row.string(Stations.STATIONCLASS));
        hash.put(Stations.ONUM.columnName(), lookup().OPERATORS.getValue().get(row.value(Stations.ONUM)));
        hash.put(Stations.LOCALID.columnName(), row.string(Stations.LOCALID));
        hash.put(Stations.LATITUDE.columnName(), row.value(Stations.LATITUDE));
        hash.put(Stations.LONGITUDE.columnName(), row.value(Stations.LONGITUDE));
        hash.put(Stations.ELEVATION.columnName(), row.value(Stations.ELEVATION));
        hash.put(Stations.REGION.columnName(), lookup().REGION_DISPLAY.getValue().get(row.value(Stations.REGION)));
        hash.put(Stations.NATION.columnName(), lookup().NATION_DISPLAY.getValue().get(row.value(Stations.NATION)));
        hash.put(Stations.STATUS.columnName(), Hardcoded.getStatusDisplayName(row.value(Stations.STATUS)));
        hash.put("dataset_" + DataSets.QUALITY.columnName(), datasetQuality);
        hash.put("dataset_" + DataSets.SOURCE.columnName(), datasetSource);
        hash.put("dataset_" + DataSets.SOURCEID.columnName(), datasetSourceID);
        hash.put("dataset_" + DataSets.METADATAURL.columnName(), datasetMetadata);
        hash.put(Stations.METADATA.columnName(), row.string(Stations.METADATA));
        hash.put(Stations.DATASETID.columnName(), lookup().DATA_SET_NAMES.getValue().get(row.value(Stations.DATASETID)));
        hash.put(Stations.CNT.columnName(), row.value(Stations.CNT));
        return hash;
    }


}
