package pscp.restlet.resource;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import pscp.restlet.test.RestletTestCase;

/**
 *
 * @author iws
 */
public class QuickTest extends RestletTestCase {

    public QuickTest(String testName) {
        super(testName);
    }

    public void testServiceDirectory() throws Exception {
        Response resp = get("/");
        Document dom = resp.getEntityAsDom().getDocument();
        NodeList links = (NodeList) XPathFactory.newInstance().newXPath().evaluate("//li/a", dom, XPathConstants.NODESET);
        for (int i = 0; i < links.getLength(); i++) {
            String href = links.item(i).getAttributes().getNamedItem("href").getNodeValue();
            if (href.indexOf('{') >= 0) {
                continue;
            }
            long time = System.currentTimeMillis();
            resp = get(href);
            assertEquals("Non OK response from " + href, Status.SUCCESS_OK, resp.getStatus());
            String text = resp.getEntity().getText();
            if (text != null) {
                System.out.println("read " + text.length() + " in " + (System.currentTimeMillis() - time));
            }
            // skip descent into forms
            if (!href.endsWith("form/")) {
                // wrap this since we sometimes deal in document fragments
                text = "<html>" + text + "</html>";
                Document dom2 = null;
                try {
                    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                    dom2 = builder.parse(new InputSource(new StringReader(
                            text)));
                } catch (Exception ex) {
                    // some of the responses are technically invalid xml/html since they
                    // contain non-encoded '&' characters (seems to be only the metadata field that sometimes has URLS)
                    ex.printStackTrace();
                    continue;
                }
                NodeList links2 = (NodeList) XPathFactory.newInstance().newXPath().evaluate("//li/a", dom2,
                        XPathConstants.NODESET);
                for (int j = 0; j < links2.getLength(); j++) {
                    String href2 = links2.item(j).getAttributes().getNamedItem("href").getNodeValue();
                    Reference ref = new Reference(href2);
                    // skip external links - these may or may not exist
                    if (base.getHostDomain().equals(ref.getHostDomain())) {
                        time = System.currentTimeMillis();
                        resp = get(ref);
                        assertEquals("On page, " + href + ",non OK response from " + href2, Status.SUCCESS_OK, resp.
                                getStatus());
                        text = resp.getEntity().getText();
                        if (text != null) {
                            System.out.println("read " + text.length() + " in " + (System.currentTimeMillis() - time));
                        }
                    } else {
                        System.out.println("SKIPPING " + ref);
                    }
                }
            }
        }
    }
}
