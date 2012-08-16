
package dao.pricip;

import dao.pscp.Products;
import dao.pscp.Stations;
import dao.pscp.Regions;
import dao.pscp.Nations;
import dao.pscp.Contacts;
import dao.pscp.ProductRevisions;
import dao.pscp.ProductTypes;
import dao.pscp.ProductLinks;
import dao.pscp.ProductNames;
import dao.Column;
import dao.DAOCollection;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.DataSets;
import dao.pscp.postgres.PGDaoFactory;
import junit.framework.TestCase;
import pscp.restlet.PSCPDataSource;

/**
 * Just power through a bunch of DAO code and ensure stuff is read relatively fast with
 * no exceptions.
 * @author iws
 */
public class BruteForceDAOTest extends TestCase {
    
    public BruteForceDAOTest(String testName) {
        super(testName);
    }

    public void testRead() throws Exception {
        DAOCollection daos = PGDaoFactory.createPGDaoFactory(PSCPDataSource.createDataSource()).create();
        read(daos.get(ProductTypes.class).readGroups());
        read(daos.get(Contacts.class).readContacts());
        read(daos.get(Nations.class).read());
        read(daos.get(ProductLinks.class).readProducts());
        read(daos.get(ProductRevisions.class).readAll());
        read(daos.get(ProductNames.class).read());
        read(daos.get(Products.class).read());
        read(daos.get(Regions.class).readRegions());
        read(daos.get(Stations.class).readStations(null, null));
        read(daos.get(Stations.class).readUniqueStationTypes());
        read(daos.get(DataSets.class).read());
    }

    private void read(RowIterator rows) throws DAOException {
        long time = System.currentTimeMillis();
        int cnt = 0;
        while (rows.hasNext()) {
            Row r = rows.next();
            for (Column c: rows.columns()) {
                Object obj = r.value(c);
                String objText = obj == null ? "" : obj.toString();
                String text = r.string(c);
                assertEquals(text, objText);
            }
            cnt++;
        }
        System.out.printf("read %s in : %s\n",cnt,(System.currentTimeMillis() - time));
    }
}
