package pscp.restlet;

import java.text.*;
import java.util.*;


/**
 *
 * @author rla
 * 
 * Provide a simplistic version number suitable for printing out on the 
 * service summary page. 
 */
public class Version {
    
    private static final int majorVersionNumber = 1;
    private static final int minorVersionNumber = 2;
    private static final int revisionNumber = 3;
    
    private static Date versionDate;
    
    static {
        Calendar gc = GregorianCalendar.getInstance();
        gc.set(2012, 3, 20);
        versionDate = gc.getTime();
    }
    
    public static String getVersion() {
        return majorVersionNumber+"."+minorVersionNumber+"."+revisionNumber;
    }
    
    public static String getVersionDate() {
        DateFormat df = DateFormat.getDateInstance(DateFormat.LONG);
        return df.format(versionDate);
    }
    
}
