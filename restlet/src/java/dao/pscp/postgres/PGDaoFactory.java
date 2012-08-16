
package dao.pscp.postgres;

import dao.DAO;
import dao.DAOFactory;
import dao.pscp.*;
import java.util.Map;
import javax.sql.DataSource;

/**
 *
 * @author iws
 */
public class PGDaoFactory {

    public static DAOFactory createPGDaoFactory(DataSource ds) {
        Map<Class<? extends DAO>,Class<? extends DAO>> classes = DAOFactory.createConfigMap();
        classes.put(Contacts.class,Contacts.class);
        classes.put(Regions.class,Regions.class);
        classes.put(Products.class,Products.class);
        classes.put(ProductRevisions.class,ProductRevisions.class);
        classes.put(ProductLinks.class,ProductLinks.class);
        classes.put(Stations.class,Stations.class);
        classes.put(ProductTypes.class,ProductTypes.class);
        classes.put(ProductNames.class,ProductNames.class);
        classes.put(Operators.class,Operators.class);
        classes.put(Nations.class,Nations.class);
        classes.put(DataSets.class,DataSets.class);
        classes.put(StationData.class,StationData.class);
        classes.put(ProductPage.class,ProductPage.class);
        classes.put(Citations.class,Citations.class);
        classes.put(Topics.class,Topics.class);
        return DAOFactory.create(ds, classes);
    }
}
