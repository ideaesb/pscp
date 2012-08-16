
package pscp.restlet.resource;

import java.util.List;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;
import pscp.restlet.util.BackgroundExecutor.Batch;

/**
 *
 * @author iws
 */
public class BatchMonitorResource extends BaseResource {
    public static final String PSCP_DESCRIPTION = "Show any background batch processing jobs" +
            " and provide a link to cancel";

    @Override
    protected void initResource() {
        configureVariants(MediaType.TEXT_HTML);
    }

    @Override
    public void acceptRepresentation(Representation entity) throws ResourceException {
        Form form = new Form(entity);
        List<Batch> batches = getJobExecutor().listBatches();
        int id = Integer.parseInt(form.getFirstValue("id"));
        for (Batch b: batches) {
            if (b.hashCode() == id) {
                b.cancel();
                break;
            }
        }
    }

    @Override
    public boolean allowPost() {
        return true;
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        StringBuffer sb = new StringBuffer();
        List<Batch> batches = getJobExecutor().listBatches();
        for (Batch b : batches) {
            sb.append("<div>");
            sb.append(b.getName());
            sb.append(' ');
            sb.append(b.getJobsRemaining());
            sb.append(" jobs remaining");
            sb.append("<form method='POST'><input type='submit' value='cancel'/><input name='id' type='hidden' value='" + b.hashCode() + "'/></form>");
            sb.append("</div>");
        }
        return new StringRepresentation(sb,MediaType.TEXT_HTML);
    }

}
