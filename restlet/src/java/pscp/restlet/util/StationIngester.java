package pscp.restlet.util;

import dao.Column;
import dao.DAOException;
import dao.DAOFactory;
import dao.Pair;
import dao.Row;
import dao.pscp.DataSets;
import dao.pscp.Operators;
import dao.pscp.StationData;
import dao.pscp.Stations;
import dao.pscp.postgres.PGDaoFactory;
import java.io.File;
import java.io.IOException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import javax.sql.DataSource;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import pscp.restlet.PSCPDataSource;
import pscp.restlet.util.ExcelMultiTableReader.TableEntry;

/**
 *
 * @author iws
 */
public class StationIngester extends Ingester {

    private boolean allowOperatorDefinitions = false;
    private boolean allowUpdate = false;
    private String sheetName;
    private Map<String, String> regionDisplayNames;
    private LookupCache lookup;

    @Override
    protected void doIngest() throws DAOException, IOException {
        lookup = new LookupCache(daoFactory());
        regionDisplayNames = lookup.REGION_DISPLAY.getValue();

        Workbook workbook;
        pushLoggingScope("Ingesting " + inputFile.getName());
        try {
            workbook = Workbook.getWorkbook(inputFile);
        } catch (BiffException ex) {
            throw new IOException("Error opening spreadsheet", ex);
        }
        if (workbook.getNumberOfSheets() == 1) {
            ingest(workbook.getSheet(0));
        } else {
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Sheet sheet = workbook.getSheet(i);
                if (regionDisplayNames.containsKey(sheet.getName())) {
                    pushLoggingScope("Reading sheet : " + sheet.getName());
                    try {
                        ingest(sheet);
                    } finally {
                        popLoggingScope();
                    }
                } else {
                    warn("Not processing sheet " + sheet.getName() + ", does not match any regions");
                }
            }
        }
        popLoggingScope();
    }

    @Override
    protected File getLogFile() {
        return new File(System.getProperty("java.io.tmpdir"), "station-ingest-" + inputFile.getName() + ".log");
    }

    private void ingest(Sheet sheet) throws IOException, DAOException {
        sheetName = sheet.getName();
        ExcelMultiTableReader reader = new ExcelMultiTableReader();
        try {
            reader.read(sheet, true);
        } catch (Exception ex) {
            throw new IOException("Error reading spreadsheet", ex);
        }
        TableEntry stations = reader.getTable("station");
        Map<String, String> metadata = reader.getMetaData();
        StationDataSetIngester ingester = new StationDataSetIngester(stations);
        ingester.setMetaData(metadata);
        ingester.ingest();
    }

    public void setAllowUpdate(boolean allowUpdate) {
        this.allowUpdate = allowUpdate;
    }

    public void setAllowOperatorDefinitions(boolean allowOperatorDefinitions) {
        this.allowOperatorDefinitions = allowOperatorDefinitions;
    }

    private Map<String, Integer> index(TableEntry e) {
        Map<String, Integer> index = new HashMap<String, Integer>();
        for (int i = 0; i < e.columns.size(); i++) {
            String colname = e.columns.get(i);
            colname = colname.trim().toLowerCase().replace(" ", "");
            index.put(colname, i);
        }
        return index;
    }

    private List<String> findMissingFields(String[] row, String[] skipList) {
        List<String> missing = new ArrayList<String>();
        for (int i = 0; i < row.length; i++) {
            if (skipList[i] != null) {
                if (row[i] == null || row[i].trim().length() == 0) {
                    missing.add(skipList[i]);
                }
            }
        }
        return missing;
    }

    public void setInput(File file) {
        this.inputFile = file;
    }

    static class MissingColumnException extends RuntimeException {

        public MissingColumnException(String colname) {
            super(colname);
        }
    }

    abstract class TableIngester {

        final TableEntry table;
        final Map<String, Integer> index;
        final Set<String> allowBlankColumnNames;
        final Map<String, String> metadata;
        private int currentRow;
        private String[] row;
        String action;

        TableIngester(TableEntry e) {
            this.table = e;
            allowBlankColumnNames = new HashSet<String>();
            index = index(e);
            metadata = new HashMap<String, String>();
        }

        String action() {
            return action == null ? "ingesting" : action;
        }

        void setMetaData(Map<String, String> metadata) {
            for (String k : metadata.keySet()) {
                this.metadata.put(k.toLowerCase().replace(" ", ""), metadata.get(k).trim());
            }
        }

        final int currentRow() {
            return currentRow;
        }

        final boolean ingest() throws DAOException {
            boolean success = true;
            List<String[]> data = table.data;
            String[] skipList = table.columns.toArray(new String[0]);
            for (int i = 0; i < skipList.length; i++) {
                String simpleName = skipList[i].toLowerCase().replace(" ", "");
                if (allowBlankColumnNames.contains(simpleName)) {
                    skipList[i] = null;
                }
            }
            for (int i = 0; i < data.size(); i++) {
                currentRow = i;
                row = data.get(i);
                List<String> missing = findMissingFields(row, skipList);
                if (missing.size() > 0) {
                    errTable("has missing values : " + missing);
                    continue;
                }
                Savepoint save = daos().savepoint();
                try {
                    ingestRow();
                } catch (MissingColumnException mce) {
                    // we're done
                    errTable("table is missing one or more columns : " + mce.getMessage() + ", this table will not be processed.");
                    success = false;
                    break;
                } catch (DAOException dao) {
                    logger().log(Level.WARNING, "Error ingesting row " + currentRow(), dao);
                    errTable("Error " + action());
                    daos().rollback(save);
                }
            }
            return success;
        }

        Boolean parseBoolean(String val) {
            Boolean parsed = null;
            val = val.toLowerCase();
            if (val.equals("y") || val.equals("yes")) {
                parsed = Boolean.TRUE;
            } else if (val.equals("n") || val.equals("no")) {
                parsed = Boolean.FALSE;
            }
            return parsed;
        }

        final String col(String name) {
            String value = metadata.get(name);
            if (value == null) {
                Integer idx = index.get(name);
                if (idx != null) {
                    value = row[idx].trim();
                } else {
                    System.out.println(metadata);
                    System.out.println(index);
                    throw new MissingColumnException(name);
                }
            }
            return value;
        }

        final Pair pair(Column col, String name) {
            String value = col(name);
            return col.parsePair(value);
        }

        final Pair pair(Column col) {
            return pair(col, col.columnName());
        }

        final void warnTable(String msg) {
            warn("[" + sheetName + "] " + table.name + " at row " + table.resolveExcelRow(currentRow) + " : " + msg);
        }

        final void errTable(String msg) {
            err("[" + sheetName + "] " + table.name + " at row " + table.resolveExcelRow(currentRow) + " : " + msg);
        }

        abstract void ingestRow() throws DAOException;
    }

    class StationDataSetIngester extends TableIngester {

        private final Map<String, Integer> onums;
        private final Stations stations;
        private final DataSets dataSets;
        private final StationData stationData;
        private int stationID;
        private int dataSetID;

        public StationDataSetIngester(TableEntry entry) throws DAOException {
            super(entry);
            onums = daos().get(Operators.class).readReverseLookup();
            stations = daos().get(Stations.class);
            dataSets = daos().get(DataSets.class);
            stationData = daos().get(StationData.class);
            allowBlankColumnNames.add("blank");
            allowBlankColumnNames.add("stationtype");
            allowBlankColumnNames.add("");
        }

        @Override
        void ingestRow() throws DAOException {
            stationID = dataSetID = -1;
            ingestStation();
            if (stationID >= 0) {
                ingestDataSet();
            }
        }

        void ingestDataSet() throws DAOException {
            if (dataSetID >= 0 && !allowUpdate) {
                warnTable("Dataset already exists and cannot update, skipping");
                return;
            }
            String source = col("source");
            String sourceid = col("sourceid");
            String metadata = col("sometadataurl");
            Boolean compliant = parseBoolean(col("sometadatacompliant"));
            if (compliant == null && !isMissingValue(col("sometadatacompliant"))) {
                warnTable("Invalid yes/no value for sometadatacompliant " + col("sometadatacompliant"));
                return;
            }
            boolean derived = false;
            int quality = 0;
            try {
                quality = Integer.parseInt(col("qualitylevel"));
                if (quality < 1 || quality > 3) {
                    errTable("Invalid quality level - " + col("qualitylevel"));
                    return;
                }
            } catch (NumberFormatException nfe) {
                errTable("Invalid quality level - " + col("qualitylevel"));
                return;
            }
            Pair[] pairs = new Pair[]{
                DataSets.SOURCE.pair(source),
                DataSets.SOURCEID.pair(sourceid),
                DataSets.METADATAURL.pair(metadata),
                DataSets.COMPLIANT.pair(compliant),
                DataSets.DERIVED.pair(derived),
                DataSets.QUALITY.pair(quality)
            };
            if (dataSetID > 0) {
                action = "updating dataset";
                info("Updated data set");
                daos().get(DataSets.class).update(dataSetID, pairs);
            } else {
                action = "inserting dataset";
                dataSetID = daos().get(DataSets.class).insert(pairs);
                Stations stationsDAO = daos().get(Stations.class);
                stationsDAO.updateStation(stationID, Stations.DATASETID.pair(dataSetID));
                info("Added data set for station");
            }
        }

        boolean isMissingValue(String value) {
            return value.equals("???") || value.equals("N/A");
        }

        void ingestStation() throws DAOException {
            String operator = col("operator");
            Integer onum = onums.get(operator);
            if (onum == null) {
                if (!allowOperatorDefinitions) {
                    warnTable("Cannot locate operator " + operator);
                    return;
                } else {
                    onum = daos().get(Operators.class).insertOperator(operator);
                    onums.put(operator, onum);
                    info("Created operator entry for " + operator);
                }
            }
            String stationName = col("stationname");
            String nation = col("country");
            String process = col("process");
            String sourceid = col("sourceid");
            String region = col("region");
            // @revisit - breadth of key used to find existing station (should be as small as possible)
            Row existingStationData = stationData.find(
                    StationData.PROCESS.pair(process),
                    StationData.SOURCEID.pair(sourceid));

            if (existingStationData != null) {
                stationID = existingStationData.value(StationData.STATIONID);
            }

            if (stationID >= 0 && !allowUpdate) {
                // we're not going to update, but still need to resolve dataset
                Row station = stations.readStation(stationID).next();
                dataSetID = station.value(Stations.DATASETID);
                warnTable("Record for " + stationName + "," + sourceid + " already exists, not updating information");
                return;
            }

            String status = "U";
            String active = col("active").toLowerCase();
            if (active.equals("y") || active.equals("yes")) {
                status = "A";
            } else if (active.equals("n") || active.equals("no")) {
                status = "I";
            } else if (active.equals("u") || active.equals("???")) {
                status = "U";
            } else {
                warnTable("Unable to comprehend value specified for 'active' field:" + active);
            }
            String stationType = col("stationtype");
            if (stationType.length() == 0) {
                stationType = "???";
            }
            Double elevation = null;
            String val = col("elevation");
            try {
                if (!isMissingValue(val)) {
                    elevation = Double.parseDouble(val);
                }
            } catch (NumberFormatException nfe) {
                warnTable("Invalid elevation value, need number or N/A, got '" + val + "'");
            }
            Pair[] values = new Pair[]{
                Stations.STATIONNAME.pair(stationName),
                pair(Stations.STATIONTYPE),
                pair(Stations.REGION),
                Stations.NATION.pair(nation),
                pair(Stations.LOCALID, "operatorid"),
                pair(Stations.LATITUDE),
                pair(Stations.LONGITUDE),
                Stations.ELEVATION.pair(elevation),
                Stations.ONUM.pair(onum),
                Stations.STATUS.pair(status),
                pair(Stations.METADATA, "stmetadataurl"),
                pair(Stations.PROCESS),
                pair(Stations.STATIONCLASS)
            };

            if (stationID >= 0) {
                action = "updating station";
                stationID = existingStationData.value(StationData.STATIONID);
                stations.updateStation(stationID, values);
                Row station = stations.readStation(stationID).next();
                dataSetID = station.value(Stations.DATASETID);
                info("Updated station " + stationName + "," + sourceid);
            } else {
                action = "inserting station";
                stationID = stations.insertStation(values);
                info("Added station " + stationName + "," + sourceid);
            }
        }
    }
    
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            args = new String[]{
                        "PRICIP 09 DDP HR station list_v5.xls"
                    };
        }
        DataSource ds = PSCPDataSource.createDataSource("williamsfork", "pscp", "postgres", "admin123");
        DAOFactory daoFactory = PGDaoFactory.createPGDaoFactory(ds);
        for (int i = 0; i < args.length; i++) {
            String loc = args[i];

            StationIngester ingester = new StationIngester();
            ingester.allowOperatorDefinitions = true;
            List<LogRecord> msgs = null;
            ingester.setDaoFactory(daoFactory);
            ingester.setInput(new File(loc));
            try {
                msgs = ingester.ingest();
                ingester.commit();
            } catch (Exception ex) {
                ex.printStackTrace();
                ingester.rollback();
            }
            if (msgs != null) {
                SimpleFormatter fmt = new SimpleFormatter();
                for (int j = 0; j < msgs.size(); j++) {
                    System.out.println(fmt.format(msgs.get(j)));
                }
            }
        }
    }
}
