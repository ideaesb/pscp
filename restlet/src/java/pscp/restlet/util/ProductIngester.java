package pscp.restlet.util;

import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.Rows;
import dao.Table;
import dao.Tables;
import dao.pscp.DataSets;
import dao.pscp.ProductTypes;
import dao.pscp.ProductRevisions;
import dao.pscp.ProductNames;
import dao.pscp.Products;
import dao.pscp.Stations;
import dao.pscp.postgres.PGDaoFactory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import pscp.restlet.PSCPDataSource;

/**
 * @author iws
 */
public class ProductIngester extends Ingester {

    private File dest;
    private UUID submitter;
    private Map<String, Integer> groupLookup;
    private Map<Integer, Map<String, Row>> groupTypeLookup;
    private Map<Integer, String> dataSetLookup;
    private Table stations;
    private UUID productContact;
    private boolean createProductDefinitions;
    private String productSource;
    private boolean autoRevision;
    private boolean extractAsynchronously;
    private List<Runnable> extractTasks;
    private MD5Sum summer;
    private boolean scaleProducts = true;

    public ProductIngester() {
        groupTypeLookup = new HashMap<Integer, Map<String, Row>>();
    }

    public BackgroundExecutor.BatchDefinition getExtractTasks() throws IOException, DAOException {
        BackgroundExecutor.BatchDefinition batch = null;
        if (extractTasks.size() > 0) {
            reopenDAOs();
            final ZipFile zip = new ZipFile(inputFile);
            for (int i = 0; i < extractTasks.size(); i++) {
                ExtractTask t = (ExtractTask) extractTasks.get(i);
                t.zip = zip;
            }
            batch = new BackgroundExecutor.BatchDefinition("Extract products from " + inputFile.getName(), extractTasks, new Runnable() {
                public void run() {
                    try {
                        commit();
                    } catch (DAOException ex) {
                        logger().log(Level.SEVERE, "Error commiting revisions", ex);
                    }
                    try {
                        zip.close();
                    } catch (IOException ex) {
                        logger().log(Level.SEVERE, "Error closing zip file", ex);
                    }
                }
            });
        }
        return batch;
    }

    @Override
    protected File getLogFile() {
        return new File(System.getProperty("java.io.tmpdir"),"product-ingest-" + inputFile.getName() + ".log");
    }

    public void setExtractAsynchronously(boolean extractSynchronously) {
        this.extractAsynchronously = extractSynchronously;
    }

    public void setProductSource(String productSource) {
        this.productSource = productSource;
    }

    public void setProductContact(UUID contact) {
        this.productContact = contact;
    }

    public void setDest(File dest) {
        this.dest = dest;
    }

    public void setAutoRevision(boolean autoRevision) {
        this.autoRevision = autoRevision;
    }

    public void setSubmitter(UUID submitter) {
        this.submitter = submitter;
    }

    public void setAllowCreateProductDefinitions(boolean allow) {
        this.createProductDefinitions = allow;
    }

    // @todo combine this functionality w/ that in ProductRevisionForm !
    private void addProduct(String group, String type, String productName, ZipFile zip, ZipEntry entry) throws
            DAOException, IOException {
        // @allow for comment in like named file
        String newID = UUID.randomUUID().toString();
        int dot = productName.lastIndexOf('.');
        if (dot <= 0) {
            warn("Unable to ingest product '" + entry.getName() + "', it is either missing a file extension or has no name...");
            return;
        }
        String ext = productName.substring(dot).toLowerCase();
        String stationName = productName.substring(0, dot).trim();
        String newFile = newID + ext;
        File productDest = new File(dest, newFile);
        if (productDest.exists()) {
            throw new RuntimeException("The impossible happened " + productDest.getAbsolutePath() + " already exists");
        }
        Integer productID = resolveProductID(group, type, stationName);
        if (productID == null) {
            warn("Unable to locate a product definition for " + group + "," + type + ",'" + stationName + "'");
            return;
        }
        if (!dryRun) {
            if (extractAsynchronously) {
                extractTasks.add(new ExtractTask(entry, productDest, newID,productID,newFile,productName,group,type));
            } else {
                writeProductAndThumb(zip, entry, productDest, newID,productID,newFile,productName,group,type);
            }
        }
    }

    private String computeMD5(ZipFile zip, ZipEntry entry) throws IOException {
        if (summer == null) {
            summer = new MD5Sum();
        }
        String md5 = null;
        InputStream in = zip.getInputStream(entry);
        try {
            md5 = summer.md5sum(in);
        } finally {
            in.close();
        }
        return md5;
    }

    public void setScaleProducts(boolean b) {
        this.scaleProducts = b;
    }

    class ExtractTask implements Runnable {

        private final String entry;
        private final File productDest;
        private final String newID;
        private ZipFile zip;
        private final Integer productID;
        private final String newFile;
        private final String productName;
        private final String group;
        private final String type;

        private ExtractTask(ZipEntry entry, File productDest, String newID, Integer productID, String newFile,
                String productName, String group, String type) {
            this.entry = entry.getName();
            this.productDest = productDest;
            this.newID = newID;
            this.productID = productID;
            this.newFile = newFile;
            this.productName = productName;
            this.group = group;
            this.type = type;
        }

        public void run() {
            try {
                writeProductAndThumb(zip, zip.getEntry(entry), productDest, newID,productID,newFile,productName,group,type);
            } catch (Exception ex) {
                logger().log(Level.SEVERE, "Error creating thumbnail", ex);
            }
        }
    }

    private void writeProductAndThumb(ZipFile zip, ZipEntry entry, final File productDest, final String newID,
            Integer productID,String newFile,String productName,String group,String type) throws
            IOException, DAOException {
        String md5Sum = computeMD5(zip,entry);
        String comment = "Added by ingester";
        ProductRevisions dao = daos().get(ProductRevisions.class);
        Integer existingRevision = dao.find(productID, md5Sum);
        if (existingRevision != null) {
            info("Not making new revision for existing product");
            return;
        }
        int revision = dao.addRevision(productID, newFile, productName, comment, submitter,md5Sum);
        String fullProductPath = group + "/" + type + "/" + productName;
        if (autoRevision) {
            daos().get(Products.class).updateProduct(productID, Products.REV.pair(revision));
            info("Added product revision for %s and set it as current", fullProductPath);
        } else {
            info("Added product revision for %s - this is not the current version published", fullProductPath);
        }

        // at this point, only IOExceptions can occur.
        IOException failure = null;
        File thumbnail = null;
        try {
            writeProduct(zip, entry, productDest);
            logger().info("Wrote product to " + productDest.getAbsolutePath());
        } catch (IOException ioe) {
            failure = ioe;
        }
        if (failure == null && scaleProducts) {
            try {
                thumbnail = writeThumb(productDest, newID);
            } catch (Exception ex) {
                failure = new IOException("Error creating scaled image", ex);
            }
        }
        if (failure != null) {
            delete(productDest);
            delete(thumbnail);
            throw failure;
        }
    }

    private File writeThumb(File productDest, String newID) throws Exception {
        ImageScaler scaler = new ImageScaler();
        scaler.setBounds(256, 256);
        scaler.setInput(productDest);
        File thumbnail = new File(dest, newID + "_scaled.gif");
        scaler.setOutput(thumbnail);
        scaler.setHQMethod(true);
        scaler.scale();
        return thumbnail;
    }

    private void writeProduct(ZipFile zip, ZipEntry entry, File newFile) throws IOException {
        if (newFile.exists()) {
            throw new IOException("NOT OVERWRITING FILE " + newFile.getAbsolutePath());
        }
        FileOutputStream fout = new FileOutputStream(newFile);
        InputStream in = zip.getInputStream(entry);
        byte[] buf = new byte[8192];
        int r = 0;
        while ((r = in.read(buf)) > 0) {
            fout.write(buf, 0, r);
        }
        fout.close();
        in.close();
    }

    private Integer resolveProductID(String group, String type, String stationName) throws DAOException {
        Integer groupID = groupLookup.get(group);
        Row typeRow = null;
        Integer station = null;
        Integer dataSet = null;
        stationName = stationName.toLowerCase();
        Integer productID = null;
        if (groupID != null) {
            String cleanedType = type.replaceAll("_|\\s", "").toLowerCase();
            typeRow = getTypeLookup(groupID).get(cleanedType);
            String process = typeRow == null ? null : typeRow.string(ProductNames.PROCESS);
            if (typeRow != null) {
                for (Row r : stations) {
                    if (process.equalsIgnoreCase(r.string(Stations.PROCESS))) {
                        String sname = r.string(Stations.STATIONNAME);
                        String sid = r.string(Stations.LOCALID);
                        Integer stationDataSet = r.value(Stations.DATASETID);
                        String dataSetSourceID = stationDataSet == null ? null : dataSetLookup.get(stationDataSet);
                        if (stationName.equalsIgnoreCase(sname)
                                || stationName.equalsIgnoreCase(sid)
                                || stationName.equalsIgnoreCase(dataSetSourceID)) {
                            station = r.value(Stations.STATIONID);
                            dataSet = r.value(Stations.DATASETID);
                            break;
                        }
                    }
                }
            } else {
                warn("Could not find type entry for group " + groupID + ", " + type);
            }
        } else {
            warn("Could not find group " + group);
        }
        if (group != null) {
            if (station != null && dataSet != null) {
                RowIterator rows = daos().get(Products.class).readProductsAtStation(station);
                Integer typeID = typeRow.value(ProductNames.NAMEID);
                while (rows.hasNext()) {
                    Row r = rows.next();
                    if (typeID.equals(r.value(Products.NAMEID))) {
                        productID = r.value(Products.ID);
                        break;
                    }
                }
                if (productID == null) {
                    if (createProductDefinitions) {
                        Products dao = daos().get(Products.class);
                        typeID = typeRow.value(ProductNames.NAMEID);
                        if (productSource == null) {
                            productSource = "???";
                        }
                        productID = dao.insertProduct("DDP", "HC", station, productSource, productContact, typeID, dataSet,
                                null);
                        info("Created product entry for " + stationName + ", " + group + " " + type);
                    } else {
                        warn("Product not found and not permitted to create defintion");
                    }
                }
            } else {
                if (station == null) {
                    warn("Unable to locate station " + stationName);
                }
                if (dataSet == null) {
                    warn("Unable to locate dataset for " + stationName);
                }
            }
        }
        return productID;
    }

    private Map<String, Row> getTypeLookup(Integer groupID) throws DAOException {
        Map<String, Row> typeLookup = groupTypeLookup.get(groupID);
        if (typeLookup == null) {
            typeLookup = daos().get(ProductNames.class).readReverseTypeNameLookup(groupID);
            for (String k : new ArrayList<String>(typeLookup.keySet())) {
                typeLookup.put(k.toLowerCase().replace(" ", ""), typeLookup.get(k));
            }
            groupTypeLookup.put(groupID, typeLookup);
        }
        return typeLookup;
    }

    private void delete(File dest) {
        if (dest.exists()) {
            if (!dest.delete()) {
                logger().warning("Unable to delete upload product on transaction abort : " + dest.getAbsolutePath());
            }
        }
    }

    private void doIngest(ZipFile zip, Enumeration<? extends ZipEntry> entries) throws IOException {
        while (entries.hasMoreElements()) {
            ZipEntry e = entries.nextElement();
            if (!e.isDirectory()) {
                String[] parts = e.getName().split("/");
                if (parts.length != 3) {
                    warn("invalid path entry : " + e.getName());
                    continue;
                }
                String group = parts[0];
                String type = parts[1];
                String station = parts[2];
                try {
                    pushLoggingScope("Ingesting " + e.getName());
                    addProduct(group, type, station, zip, e);
                } catch (DAOException daoe) {
                    warn("Error adding product " + daoe.getMessage());
                    logger().log(Level.SEVERE, "DAO ERROR", daoe);
                } finally {
                    popLoggingScope();
                }
            }
        }

    }

    protected void doIngest() throws IOException, DAOException {
        if (extractAsynchronously) {
            extractTasks = new ArrayList<Runnable>();
        }
        ZipFile zip = new ZipFile(inputFile);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        stations = Tables.table(daos().get(Stations.class).readStations(null, null));
        groupLookup = Rows.lookup(daos().get(ProductTypes.class).readGroups(), ProductTypes.NUM, ProductTypes.ID);
        dataSetLookup = Rows.lookup(daos().get(DataSets.class).read(), DataSets.DATASETID, DataSets.SOURCEID);
        try {
            pushLoggingScope("ingesting " + inputFile.getName());
            doIngest(zip, entries);
        } finally {
            zip.close();
        }
    }

    public static void main(String[] args) throws Exception {
        ProductIngester ingester = new ProductIngester();
        ingester.setInputFile(new File(args[0]));
        ingester.setDest(new File("product-ingest/ingested"));
        ingester.setSubmitter(UUID.fromString("538dd487-29ef-49d0-b378-e4c50adcc2fc"));
        ingester.setProductContact(UUID.fromString("538dd487-29ef-49d0-b378-e4c50adcc2fc"));
        ingester.setDaoFactory(PGDaoFactory.createPGDaoFactory(PSCPDataSource.createDataSource("localhost", "pscp",
                "postgres", "admin123")));
        ingester.setAllowCreateProductDefinitions(true);
        ingester.setDryRun(true);
        ingester.setAutoRevision(true);
        List<LogRecord> messages = ingester.ingest();
        HLogger.dump(messages);
        ingester.rollback();
    }
}
