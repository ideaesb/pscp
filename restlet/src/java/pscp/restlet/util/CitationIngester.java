package pscp.restlet.util;

import dao.DAOException;
import dao.DAOFactory;
import dao.Pair;
import dao.Row;
import dao.Rows;
import dao.pscp.Citations;
import dao.pscp.Topics;
import dao.pscp.postgres.PGDaoFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
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
import java.util.logging.Logger;
import javax.sql.DataSource;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import pscp.restlet.PSCPDataSource;

/**
 *
 * @author iws
 */
public class CitationIngester extends Ingester {

    private LookupCache lookup;

    @Override
    protected File getLogFile() {
        return new File(System.getProperty("java.io.tmpdir"), "citation-ingest-" + inputFile.getName() + ".log");
    }

    @Override
    protected void doIngest() throws IOException, DAOException {
        lookup = new LookupCache(daoFactory());
        Workbook workbook;
        pushLoggingScope("Ingesting " + inputFile.getName());
        try {
            workbook = Workbook.getWorkbook(inputFile);
        } catch (BiffException ex) {
            throw new IOException("Error opening spreadsheet", ex);
        }
        ingest(workbook.getSheet(0));
        popLoggingScope();
    }

    private void ingest(Sheet sheet) throws DAOException {
        Map<String, Integer> columns = new HashMap<String, Integer>();
        Map<String, String> regions = new HashMap<String, String>();
        Map<String, String> themes = new HashMap<String, String>();
        Map<String, String> topics = new HashMap<String, String>();
        List<String[]> citationRows = new ArrayList<String[]>();
        List<Integer> citationLineNumbers = new ArrayList<Integer>();
        Cell[] headers = sheet.getRow(0);
        int endCitations = -1;
        for (int i = 0; i < headers.length; i++) {
            columns.put(headers[i].getContents().trim().toLowerCase(), i);
        }
        for (int i = 1; i < sheet.getRows(); i++) {
            if (sheet.getCell(0, i).getContents().trim().endsWith(":")) {
                endCitations = i;
                break;
            }
            Cell[] cells = sheet.getRow(i);
            if (cells.length == 0) {
                continue;
            }
            if (cells[0].getContents().trim().length() > 0) {
                String[] contents = new String[cells.length];
                for (int j = 0; j < contents.length; j++) {
                    contents[j] = cells[j].getContents().trim();
                }
                citationRows.add(contents);
                citationLineNumbers.add(i + 1);
            }
        }
        for (int i = endCitations; i < sheet.getRows(); i++) {
            String type = sheet.getCell(0, i).getContents().trim();
            Map<String, String> active;
            if (type.startsWith("Region")) {
                active = regions;
            } else if (type.startsWith("Theme")) {
                active = themes;
            } else if (type.startsWith("Sub topics")) {
                active = topics;
            } else {
                logger().warning("Unable to deal with section " + type);
                continue;
            }
            while (++i < sheet.getRows()) {
                String text = sheet.getCell(0, i).getContents().trim();
                if (text.length() == 0) {
                    break;
                }
                int lastSpace = text.lastIndexOf(' ');
                if (lastSpace < 0) {
                    logger().warning("Unable to deal with text " + text);
                } else {
                    active.put(text.substring(lastSpace + 1), text.substring(0, lastSpace));
                }
            }

        }

        updateRegions(regions);
        updateThemes(themes);
        updateTopics(topics);
        updateCitations(citationRows, citationLineNumbers, columns, regions, themes, topics);
    }

    private void updateRegions(Map<String, String> regions) throws DAOException {
        pushLoggingScope("Verifying Regions");
        for (String key : regions.keySet()) {
            if (key.toLowerCase().equals("all")) {
                continue;
            }
            if (lookup.REGION_DISPLAY.getValue().get(key) == null) {
                warn("Unknown region " + key);
            }
        }
        popLoggingScope();
    }

    private void updateThemes(Map<String, String> themes) {
        Set<String> keys = new HashSet<String>(Arrays.asList(new String[]{"HR", "SW", "HS", "CS"}));
        Set<String> others = new HashSet<String>(themes.keySet());
        others.removeAll(keys);
        if (others.size() > 0) {
            throw new RuntimeException("Didn't expect themes like : " + others);
        }
    }

    private void updateTopics(Map<String, String> topics) throws DAOException {
        pushLoggingScope("Topics");

        Topics dao = daos().get(Topics.class);
        for (String key : topics.keySet()) {
            info("writing topic " + key + " - " + topics.get(key));
            dao.write(key, topics.get(key));
        }
        popLoggingScope();
    }

    private void updateCitations(List<String[]> citationRows,
            List<Integer> lineNumbers,
            Map<String, Integer> columns,
            Map<String, String> regions,
            Map<String, String> themes,
            Map<String, String> topics) throws DAOException {
        String[] columnNames = new String[]{
            "Article Name",
            "Author",
            "Year published",
            "Journal",
            "Region",
            "Sub-topic",
            "Theme",
            "Citation",
            "Link"
        };
        boolean columnErrors = false;
        int[] idxs = new int[columnNames.length];
        for (int i = 0; i < columnNames.length; i++) {
            Integer idx = columns.get(columnNames[i].toLowerCase());
            if (idx != null) {
                idxs[i] = idx;
            } else {
                logger().severe("Unable to locate column " + columnNames[i]);
                columnErrors = true;
            }
        }
        if (!columnErrors) {
            Citations citations = daos().get(Citations.class);
            Map<String, Row> previousCitations = Rows.rowLookup(citations.read(), Citations.NAME);
            Map<String, Integer> names = new HashMap<String, Integer>();
            for (int i = 0; i < citationRows.size(); i++) {
                String[] row = citationRows.get(i);
                int idx = 0;
                List<Pair> pairs = new ArrayList<Pair>(columnNames.length);
                String citationName = row[idx++];
                pairs.add(Citations.NAME.pair(citationName));
                Integer existingRow = names.put(citationName, lineNumbers.get(i));
                if (existingRow != null) {
                    warn("The citation named '" + citationName + "' on line " + lineNumbers.get(i) + " is a duplicate of line " + existingRow);
                    continue;
                }
                pairs.add(Citations.AUTHOR.pair(row[idx++]));
                pairs.add(Citations.YEAR.pair(filterYear(row[idx++])));
                pairs.add(Citations.JOURNAL.pair(row[idx++]));
                String region = row[idx++];
                String theme = row[idx++];
                String topic = row[idx++];
                pairs.add(Citations.REGION.pair(region));
                pairs.add(Citations.THEME.pair(theme));
                pairs.add(Citations.TOPIC.pair(topic));
                pairs.add(Citations.CITATION.pair(row[idx++]));
                pairs.add(Citations.LINK.pair(filterLink(row[idx++], lineNumbers.get(i))));
                Row previous = previousCitations.get(citationName);
                if (previous != null) {
                    pairs.add(Citations.FILE.pair(previous.value(Citations.FILE)));
                    pairs.add(Citations.ID.pair(previous.value(Citations.ID)));
                    previousCitations.remove(citationName.toString());
                }
                // @todo region,theme,topic atom sanity
                citations.addCitation(pairs.toArray(new Pair[pairs.size()]));
            }
            if (previousCitations.size() > 0) {
                StringBuilder buf = new StringBuilder();
                for (Row r : previousCitations.values()) {
                    String file = r.string(Citations.FILE);
                    if (file.trim().length() > 0) {
                        buf.append(file).append("\n");
                    }
                    citations.delete(r.value(Citations.ID));
                }
                if (buf.length() > 0) {
                    warn("One or more previously associated files is orphaned :\n" + buf);
                }
            }
        } else {
            rollback();
        }
    }

    private String filterYear(String string) {
        // @todo agree on missing specifier (i.e. unknown, etc)
        try {
            Integer.parseInt(string);
            return string;
        } catch (NumberFormatException nfe) {
            return "";
        }
    }

    public static void main(String[] args) throws Exception {
        CitationIngester in = new CitationIngester();
        Logger.getLogger("").getHandlers()[0].setLevel(Level.OFF);
        in.inputFile = new File(args[0]);
        DataSource ds = PSCPDataSource.createDataSource("localhost");
        DAOFactory daoFactory = PGDaoFactory.createPGDaoFactory(ds);
        in.setDaoFactory(daoFactory);
        List<LogRecord> records = in.ingest();
        HLogger.dump(records);
        in.commit();
//        in.rollback();
    }

    private String filterLink(String link, int line) {
        if (link.equals("N/A")) {
            link = "";
        } else {
            try {
                new URL(link);
            } catch (MalformedURLException murle) {
                warn("Invalid link " + link + " specified for citation on line " + line);
            }
        }
        return link;
    }
}
