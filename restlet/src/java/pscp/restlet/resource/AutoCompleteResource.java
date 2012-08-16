package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Nations;
import dao.pscp.Operators;
import dao.pscp.ProductTypes;
import dao.pscp.ProductNames;
import dao.pscp.Regions;
import dao.pscp.Stations;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.restlet.resource.WriterRepresentation;
import pscp.restlet.util.RestletDAOUtils;

/**
 * @todo REFACTOR-1 this to a dao-like thing for parameters
 * @todo finish varName injection for inclusion in script headers
 * @author iws
 */
public abstract class AutoCompleteResource extends BaseResource {

    private static final Variant JSON = new Variant(MediaType.APPLICATION_JAVASCRIPT);
    private RowIterator rows;
    private final String varName;
    private final MessageFormat format;
    private final Column id;
    private final Column[] parts;
    protected final List<Column> additionalFields = new ArrayList<Column>();
    public static final String PSCP_DESCRIPTION = "Provide Autocompletion Results for UI";

    protected AutoCompleteResource(String varName, String format, Column id, Column... parts) {
        getVariants().add(JSON);
        this.varName = varName;
        this.format = format == null ? null : new MessageFormat(format);
        this.id = id;
        this.parts = parts;
    }

    @Override
    protected void initResource() {
        // this is a hack since ac resources are currently used from other resources without an
        // actual request @see REFACTOR-1 above
        if (getRequest() == null || isModified()) {
            try {
                rows = resolveRows();
                initAutoComplete();
            } catch (DAOException dao) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error fetching rows", dao);
                if (getResponse() != null) {
                    getResponse().setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            }
        } else {
            getResponse().setStatus(Status.REDIRECTION_NOT_MODIFIED);
        }
    }

    protected void initAutoComplete() throws DAOException {
    }

    protected void getArgs(Row row, Object[] args) {
        for (int i = 0; i < parts.length; i++) {
            args[i] = value(row, parts[i]);
        }
    }

    protected Object value(Row row, Column col) {
        return row.value(col);
    }

    protected int rowCount() {
        return parts.length;
    }

    protected void writeID(Writer w, Object[] values) throws IOException {
        if (format == null) {
            throw new RuntimeException("You must override writeID if you don't provide a format string");
        }
        w.write(format.format(values));
    }

    public Parameter[] asParamArray(BaseResource caller) throws DAOException {
        // don't close daos!
        useDAOs(caller);
        initResource();
        List<Parameter> p = new ArrayList<Parameter>();
        StringWriter writer = new StringWriter();
        Object[] values = new Object[rowCount()];
        while (rows.hasNext()) {
            Row next = rows.next();
            getArgs(next, values);
            try {
                writeID(writer, values);
            } catch (IOException ex) {
                // should never happen
                throw new RuntimeException(ex);
            }
            p.add(new Parameter(writer.toString(), next.string(id)));
            writer.getBuffer().delete(0, writer.getBuffer().length());
        }
        // @hack #2001
        useDAOs(null);
        return p.toArray(new Parameter[p.size()]);
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        return new WriterRepresentation(MediaType.APPLICATION_JAVASCRIPT) {

            {
                setCharacterSet(CharacterSet.ISO_8859_1);
                setModificationDate(ModifiedCache.getLastModified(AutoCompleteResource.this));
            }

            @Override
            public void write(Writer writer) throws IOException {
                try {
                    doWrite(writer);
                } catch (DAOException ex) {
                    throw new IOException("Error during write", ex);
                }
            }

            private void doWrite(Writer writer) throws IOException, DAOException {
//                writer.write("var ");
//                writer.write(varName);
//                writer.write("=[");
                writer.write("[");
                Object[] values = new Object[rowCount()];
                while (rows.hasNext()) {
                    writer.write("{\"id\":\"");
                    Row next = rows.next();
                    writer.write(next.string(id));
                    writer.write("\",");
                    getArgs(next, values);
                    writer.write("\"name\":\"");
                    writeID(writer, values);
                    writer.write("\"");
                    for (int i = 0; i < additionalFields.size(); i++) {
                        writer.write(",\"");
                        writer.write(additionalFields.get(i).columnName());
                        writer.write("\":\"");
                        writer.write(next.string(additionalFields.get(i)));
                        writer.write("\"");
                    }
                    writer.write("}");
                    if (rows.hasNext()) {
                        writer.write(',');
                    }
                }
                writer.write("];");
                writer.flush();
            }

            @Override
            public void release() {
                close();
                super.release();
            }
        };
    }

    protected abstract RowIterator resolveRows() throws DAOException;

    public static class Station extends AutoCompleteResource {
        static {
            declareDAODependency(Station.class, Stations.class);
        }

        private final Map<Integer, String> operatorCache;

        public Station() {
            super("stationAC", "{0}-{1}-{2}-{3}", Stations.STATIONID,
                    Stations.STATIONNAME, Stations.PROCESS, Stations.NATION, Stations.ONUM);
            operatorCache = lookup().OPERATORS.getValue();
        }

        @Override
        protected Object value(Row row, Column col) {
            Object value = row.value(col);
            if (col == Stations.ONUM) {
                value = operatorCache.get((Integer) value);
            }
            return value;
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(Stations.class).readStations(null, null);
        }
    }

    public static class ProductType extends AutoCompleteResource {
        static {
            declareDAODependency(ProductType.class, ProductTypes.class);
        }
        public ProductType() {
            super("productGroupAC",
                    "{0} {1}",
                    ProductTypes.ID,
                    ProductTypes.NUM, ProductTypes.NAME);
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(ProductTypes.class).readGroups();
        }
    }

    public static class ProductName extends AutoCompleteResource {
        static {
            declareDAODependency(ProductName.class, ProductNames.class);
        }
        public ProductName() {
            super("productNameAC",
                    null,
                    ProductNames.NAMEID,
                    ProductNames.PROCESS, ProductNames.QUADRANT, ProductNames.PERIOD, ProductNames.SUBJECT,
                    ProductNames.ATTRIBUTE, ProductNames.SEASON, ProductNames.TERM, ProductNames.STATISTIC,
                    ProductNames.INDICE);
            additionalFields.add(ProductNames.TYPEID);
        }

        @Override
        protected void writeID(Writer w, Object[] values) throws IOException {
            boolean wroteOne = false;
            for (int i = 0; i < values.length; i++) {
                if (values[i] != null) {
                    if (wroteOne) {
                        w.write(' ');
                    }
                    w.write(values[i].toString());
                    wroteOne = true;
                }
            }
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            List<Pair> pairs = RestletDAOUtils.parseForm(getRequest().getOriginalRef().getQueryAsForm(),
                    ProductNames.PROCESS, ProductNames.TYPEID);
            return dao(ProductNames.class).search(pairs);
        }
    }

    public static class StationType extends AutoCompleteResource {
        static {
            declareDAODependency(StationType.class, Stations.class);
        }
        public StationType() {
            super("stationTypeAC",
                    "{0}",
                    Stations.STATIONTYPE,
                    Stations.STATIONTYPE);
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(Stations.class).readUniqueStationTypes();
        }
    }

    public static class Nation extends AutoCompleteResource {
        static {
            declareDAODependency(StationType.class, Stations.class);
        }
        public Nation() {
            super("nationAC",
                    "{0}",
                    Nations.ISOCODE,
                    Nations.COUNTRYNAME);
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(Nations.class).read();
        }
    }

    public static class Region extends AutoCompleteResource {

        public Region() {
            super("regionAC",
                    "{0}",
                    Regions.CODE,
                    Regions.NAME);
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(Regions.class).readRegions();
        }
    }

    public static class Operator extends AutoCompleteResource {

        public Operator() {
            super("operatorAC",
                    "{0}",
                    Operators.ONUM,
                    Operators.OPERATOR);
        }

        @Override
        protected RowIterator resolveRows() throws DAOException {
            return dao(Operators.class).read();
        }
    }
    
}
