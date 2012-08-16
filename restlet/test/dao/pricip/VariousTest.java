
package dao.pricip;

import dao.DAOCollection;
import dao.Pair;
import dao.Row;
import dao.pscp.DataSets;
import dao.pscp.Operators;
import dao.pscp.Stations;
import dao.pscp.postgres.PGDaoFactory;
import java.math.BigDecimal;
import junit.framework.TestCase;
import pscp.restlet.PSCPDataSource;

/**
 *
 * @author iws
 */
public class VariousTest extends TestCase{

    public void testTriStateBoolean() throws Exception {
        DAOCollection daos = PGDaoFactory.createPGDaoFactory(PSCPDataSource.createDataSource()).create(true);
        DataSets dao = daos.get(DataSets.class);
        int dataset = dao.insert("TONAR","CNP",null,"",null,false,3);
        Row row = dao.read(dataset);
        assertNull("expected null 'boolean'", row.value(DataSets.COMPLIANT));
        assertEquals("", row.string(DataSets.COMPLIANT));
        daos.rollback();
        daos.close();
    }
    public void testNullableDouble() throws Exception {
        DAOCollection daos = PGDaoFactory.createPGDaoFactory(PSCPDataSource.createDataSource()).create(true);
        Stations dao = daos.get(Stations.class);
        int onum = daos.get(Operators.class).insertOperator("fizbuz");
        Pair[] pairs = new Pair[] {
            Pair.of(Stations.STATIONNAME, "Kraznat"),
            Pair.of(Stations.STATIONTYPE, "Shazbot"),
            Pair.of(Stations.LATITUDE, new BigDecimal(22)),
            Pair.of(Stations.LONGITUDE, new BigDecimal(44.)),
            Pair.of(Stations.REGION, "CNP"),
            Pair.of(Stations.NATION, "USA"),
            Pair.of(Stations.STATUS, "U"),
            Pair.of(Stations.METADATA, "Shazbot"),
            Pair.of(Stations.LOCALID, "Shazbot"),
            Pair.of(Stations.ONUM, onum),
            Pair.of(Stations.PROCESS, "SW"),
            Pair.of(Stations.ELEVATION, null), // finally, the null value
            Pair.of(Stations.DATASETID, null),
            Pair.of(Stations.STATIONCLASS, "blaznat"),
        };
        int stationid = dao.insertStation(pairs);
        Row row = dao.readStation(stationid).next();
        assertNull("expected null 'double'",row.value(Stations.ELEVATION));
        assertEquals("",row.string(Stations.ELEVATION));
        daos.rollback();
    }
}
