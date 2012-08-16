
package pscp.restlet;

import pscp.restlet.PSCPDataSource;
import junit.framework.TestCase;

/**
 *
 * @author iws
 */
public class PSCPDataSourceTest extends TestCase {
    
    public PSCPDataSourceTest(String testName) {
        super(testName);
    }

    public void testCreateDataSource() throws Exception {
        PSCPDataSource.createDataSource().getConnection();
    }

}
