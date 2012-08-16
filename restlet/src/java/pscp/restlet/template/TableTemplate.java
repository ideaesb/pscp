
package pscp.restlet.template;

import dao.Column;
import dao.RowIterator;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import java.util.List;

/**
 *
 * @author iws
 */
public class TableTemplate extends ResourceTemplate {
    private final RowIterator rows;
    private final List<Column> columns;
    private final RowTransformer transformer;

    public TableTemplate(RowIterator rows,List<Column> columns,RowTransformer transformer) {
        this.rows = rows;
        this.columns = columns;
        this.transformer = transformer == null ? TemplateModels.defaultRowTransformer(columns) : transformer;
        templateName = "table";
        templateFileName = "table-generic.html";
    }

    @Override
    protected TemplateModel buildResourceModel() {
        SimpleHash table = new SimpleHash();
        table.put("cols", TemplateModels.hashColumns(columns));
        table.put("rows", TemplateModels.tableModel(rows, transformer));
        return table;
    }

}
