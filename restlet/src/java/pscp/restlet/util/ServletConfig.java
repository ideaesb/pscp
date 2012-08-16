
package pscp.restlet.util;

import java.io.File;
import javax.xml.parsers.DocumentBuilderFactory;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.util.Series;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author iws
 */
public class ServletConfig {

    public static Series<Parameter> readServletConfig() throws Exception {
        File developmentWebXML = new File("web/WEB-INF/web.xml");
        if (!developmentWebXML.exists()) throw new RuntimeException("Unable to locate web.xml at " + developmentWebXML.getAbsolutePath());
        Document dom = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(developmentWebXML);
        Series<Parameter> params = new Form();
        NodeList nodes = dom.getElementsByTagName("context-param");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element paramElement = (Element) nodes.item(i);
            Element name = (Element) paramElement.getElementsByTagName("param-name").item(0);
            Element value = (Element) paramElement.getElementsByTagName("param-value").item(0);
            params.add(name.getTextContent().trim(), value.getTextContent().trim());
        }
        return params;
    }
}
