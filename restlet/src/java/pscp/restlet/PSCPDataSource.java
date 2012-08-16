
package pscp.restlet;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.postgresql.jdbc2.optional.PoolingDataSource;
import org.restlet.data.Parameter;
import org.restlet.util.Series;
import pscp.restlet.util.ServletConfig;

/**
 *
 * @author iws
 */
public class PSCPDataSource {

    static Map<String,DataSource> sources = new HashMap<String,DataSource>();

    static DataSource createDataSource(Series<Parameter> params) {
        String host = params.getFirstValue("pscp.db.host");
        String user = params.getFirstValue("pscp.db.user");
        String pass = params.getFirstValue("pscp.db.pass");
        String db = params.getFirstValue("pscp.db.name");
        return createDataSource(host, db, user, pass);
    }

    public static DataSource createDataSource(String host,String db,String user,String pass) {
        String key = host + db + user + pass;
        DataSource found = sources.get(key);
        if (found != null) {
            return found;
        }
        PoolingDataSource dataSource = new PoolingDataSource();
        dataSource.setDataSourceName("pscp-pool");
        try {
            String hostname = host.split("\\.")[0];
            if (InetAddress.getLocalHost().getHostName().equals(hostname)) {
                host = "localhost";
                Logger.getLogger(PSCPDataSource.class.getName()).info("Using localhost for database");
            }
        } catch (UnknownHostException ex) {
            Logger.getLogger(PSCPDataSource.class.getName()).log(Level.SEVERE, null, ex);
        }
        dataSource.setServerName(host);
        dataSource.setDatabaseName(db);
        dataSource.setUser(user);
        dataSource.setPassword(pass);
        found = dataSource;
        sources.put(key,found);
        return dataSource;
    }

    public static DataSource createDataSource(String host) throws Exception {
        Series<Parameter> readServletConfig = ServletConfig.readServletConfig();
        readServletConfig.set("pscp.db.host", host, true);
        return createDataSource(readServletConfig);
    }

    public static DataSource createDataSource() throws Exception {
        return createDataSource(ServletConfig.readServletConfig());
    }

}
