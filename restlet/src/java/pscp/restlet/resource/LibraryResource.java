
package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.Citations;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import pscp.restlet.template.RowTransformer;

/**
 *
 * @author iws
 */
public class LibraryResource extends ServiceResource {

    @Override
    protected void initServiceResource() {
    }

    @Override
    protected String getServiceTitle() {
        return "Library";
    }

    @Override
    protected List<Column> tableColumns(RowIterator rows) {
        List<Column> cols = new ArrayList<Column>(rows.columns());
        cols.remove(Citations.ID);
        cols.remove(Citations.FILE);
        return cols;
    }

    @Override
    protected RowTransformer getRowTransformer(RowIterator rows) {
        final boolean editLink = getRequest().getOriginalRef().getPath().indexOf("/admin/") >= 0;
        return new RowTransformer() {

            @Override
            public TemplateModel row(Row row) {
                SimpleSequence seq = new SimpleSequence();
                String name = htmlEncode(row.string(Citations.NAME));
                if (editLink) {
                    String basePath = getRequest().getOriginalRef().getPath();
                    if (!basePath.endsWith("/")) {
                        basePath = basePath + "/";
                    }
                    name = href( basePath + row.string(Citations.ID), name);
                }
                seq.add(name);
                seq.add(row.string(Citations.AUTHOR));
                seq.add(row.string(Citations.YEAR));
                seq.add(row.string(Citations.JOURNAL));
                seq.add(row.string(Citations.REGION));
                seq.add(row.string(Citations.TOPIC));
                seq.add(row.string(Citations.THEME));
                seq.add(htmlEncode(row.string(Citations.CITATION)));
                String link = row.string(Citations.LINK);
                if (link.length() > 0) {
                    link = href(link, "[external]");
                }
                String file = row.string(Citations.FILE);
                if (file.length() > 0) {
                    // @todo fix this path
                    file = href("/library/" + file, "[local]");
                }
                String linkCellContent;
                if (link.length() > 0 && file.length() > 0) {
                    linkCellContent = "<div>" + link + "</div><div>" + file + "</div>";
                } else {
                    linkCellContent = link.length() > 0 ? link : file;
                }
                seq.add(linkCellContent);
                return seq;
            }
        };
    }

    // @todo - this could go elsewhere
    private static String htmlEncode(String str) {
        StringBuilder b = new StringBuilder(str.length());
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c > 8000) {
                b.append("&#").append((int)c).append(';');
            } else {
                switch (c) {
                    case '<': b.append("&lt;");break;
                    case '>': b.append("&gt;");break;
                    case '&': b.append("&amp;");break;
                    case '"': b.append("&quot;");break;
                    default: b.append(c);
                }
            }
        }
        return b.toString();
    }


    @Override
    protected RowIterator resolveTable() throws DAOException {
        return dao(Citations.class).read();
    }


}
