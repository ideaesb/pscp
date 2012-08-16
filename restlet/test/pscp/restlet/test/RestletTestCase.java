package pscp.restlet.test;

import com.noelios.restlet.Engine;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;
import org.restlet.Client;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 *
 * @author iws
 */
public abstract class RestletTestCase extends TestCase {

    protected Client client;
    protected Reference base = new Reference("http://localhost:8080/pscp-api");
    private String user = "Test";
    private String pass = "TeStEr765";

    public RestletTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        client = new Client("http");
        client.start();
    }

    @Override
    protected void tearDown() throws Exception {
        client.stop();
    }

    protected Response get(Reference ref) throws Exception {
        System.out.println("get " + ref);
        Request req = new Request(Method.GET, ref);
        req.setChallengeResponse(new ChallengeResponse(ChallengeScheme.HTTP_DIGEST, user, pass));
        Response resp = client.handle(req);
        //System.out.println(resp.getEntity().getText());
        if (resp.getStatus().equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_DIGEST, user, pass);
            Form form = new Form();
            form.add("username", user);
            form.add("uri", ref.getPath());
            ChallengeRequest challenge = resp.getChallengeRequests().get(0);
            form.add(challenge.getParameters().getFirst("nonce"));
            form.add(challenge.getParameters().getFirst("realm"));
            //form.add(challenge.getParameters().getFirst("domain"));
            form.add(challenge.getParameters().getFirst("algorithm"));
            form.add(challenge.getParameters().getFirst("qop"));
            // Generate some digest values with your login, password, realm, request method, and URI path.
            String a1 = Engine.getInstance().toMd5(
                    user + ":" + form.getFirstValue("realm") + ":" + pass);
            String a2 = Engine.getInstance().toMd5(req.getMethod() + ":" + form.getFirstValue("uri"));
            // the "response" parameter is the final digest value. (its value may differ if the server sends these parameters: "cnonce" and "nc")
            String nonce = challenge.getParameters().getFirstValue("nonce");
            form.add("response", Engine.getInstance().toMd5(a1 + ":" + nonce + ":"  + a2));
            challengeResponse.setCredentialComponents(form);
            req = new Request(Method.GET, ref);
            req.setChallengeResponse(challengeResponse);
            resp = client.handle(req);
        }
        return resp;
    }

    protected Response get(String uri) throws Exception {
        Reference ref;
        if (!uri.startsWith("http")) {
            String[] parts = uri.split("/");
            List<String> segments = base.getSegments();
            Collections.addAll(segments, parts);
            if (parts.length == 0) {
                segments.add("");
            }
            ref = new Reference(base);
            ref.setSegments(segments);
            // if the original path ends with '/', ensure the new path does too
            // as setSegments has no way of ensuring this happens
            if (uri.endsWith("/") && parts.length > 0) {
                ref = new Reference(ref.toString() + "/");
            }
        } else {
            ref = new Reference(uri);
        }
        return get(ref);
    }
}
