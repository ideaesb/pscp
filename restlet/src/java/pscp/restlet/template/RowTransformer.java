package pscp.restlet.template;

import dao.Row;
import freemarker.template.TemplateModel;

public interface RowTransformer {

    TemplateModel row(Row row);
}
