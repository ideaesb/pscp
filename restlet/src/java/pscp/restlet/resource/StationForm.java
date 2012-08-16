package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Pair;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Stations;
import java.util.List;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.resource.Representation;
import pscp.restlet.URLS;
import pscp.restlet.URLS.Name;
import pscp.restlet.util.RestletDAOUtils;
import pscp.restlet.template.TemplateModels;

/**
 *
 * @author iws
 */
public class StationForm extends FormResource {

    @Override
    protected String getFormTemplate() {
        return "station-form.html";
    }

    @Override
    protected void completeFormModel(TemplateModels.FormModel form,Row row) throws DAOException {
        form.setAttributes(pairs(formAtts(), row, false));
        form.setHidden(pairs(formHidden(), row, false));
        form.setParameters(Stations.PROCESS,
                new Parameter("Strong Winds", "SW"),
                new Parameter("High Seas", "HS"),
                new Parameter("Heavy Rains", "HR"));
        URLS urls = urls();
        form.setAutocompleteURL(Stations.STATIONTYPE, urls.getURL(Name.AC_STATION_TYPE));
        form.setAutocompleteURL(Stations.NATION, urls.getURL(Name.AC_NATIONS));
        form.setParameters(Stations.REGION, new AutoCompleteResource.Region().asParamArray(this));
        form.setParameters(Stations.STATUS,
                new Parameter("Active", "A"),
                new Parameter("Inactive", "I"),
                new Parameter("Unknown", "U"));
        form.setParameters(Stations.ONUM, new AutoCompleteResource.Operator().asParamArray(this));
    }

    @Override
    protected void accept(Representation entity) throws DAOException {
        Form form = new Form(entity);
        Stations dao = dao(Stations.class);
        String stationIDValue = form.getFirstValue(Stations.STATIONID.columnName());
        List<Pair> pairs = RestletDAOUtils.parseForm(form, formAtts());
        if (stationIDValue != null) {
            int stationID = Integer.parseInt(stationIDValue);
            dao.updateStation(stationID, pairs.toArray(new Pair[pairs.size()]));
        } else {
            dao.insertStation(pairs.toArray(new Pair[pairs.size()]));
        }
        getResponse().redirectSeeOther("/services/stations");
    }

    @Override
    protected void initFormResource() {
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        RowIterator rows = null;
        String id = (String) getRequest().getAttributes().get("id");
        if (id != null) {
            rows = dao(Stations.class).readStation(Integer.parseInt(id));
        }
        return rows;
    }

    @Override
    protected String objectName() {
        return "Station";
    }

    @Override
    protected Column[] formAtts() {
        return new Column[]{
                    Stations.STATIONNAME,
                    Stations.STATIONTYPE,
                    Stations.PROCESS,
                    Stations.LOCALID,
                    Stations.NATION,
                    Stations.REGION,
                    Stations.LATITUDE,
                    Stations.LONGITUDE,
                    Stations.METADATA,
                    Stations.STATUS,
                    Stations.ONUM
                };
    }

    @Override
    protected Column[] formHidden() {
        return new Column[]{Stations.STATIONID};
    }
}
