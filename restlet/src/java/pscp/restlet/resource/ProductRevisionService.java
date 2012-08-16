/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pscp.restlet.resource;

import dao.Column;
import dao.DAOException;
import dao.Row;
import dao.RowIterator;
import dao.pscp.ProductRevisions;
import dao.pscp.Products;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateModel;
import java.util.ArrayList;
import java.util.List;
import pscp.restlet.URLS;
import pscp.restlet.template.RowTransformer;

/**
 *
 * @author en
 */
public class ProductRevisionService extends ServiceResource {
    private String id;
    private String rev;
    private Integer activeRevision;

    @Override
    protected RowTransformer getRowTransformer(RowIterator rows) {
        final URLS urls = urls();
        return new RowTransformer() {

            public TemplateModel row(Row row) {
                SimpleSequence seq = new SimpleSequence();
                String location = row.string(ProductRevisions.LOCATION);
                String link = img(urls.getProductThumb(location));
                seq.add(link);
                seq.add(row.string(ProductRevisions.CHANGED));
                seq.add(row.string(ProductRevisions.APPROVED));
                seq.add(row.string(ProductRevisions.COMMENT));
                Integer rev = row.value(ProductRevisions.REV);
                if (rev.equals(activeRevision)) {
                    seq.add("true");
                } else {
                    seq.add("");
                }
                return seq;
            }
        };
    }

    @Override
    protected List<Column> tableColumns(RowIterator rows) {
        List<Column> cols = new ArrayList<Column>();
        cols.add(ProductRevisions.LOCATION);
        cols.add(ProductRevisions.CHANGED);
        cols.add(ProductRevisions.APPROVED);
        cols.add(ProductRevisions.COMMENT);
        cols.add(new Column("Published Revision","Published Revision",Boolean.class));
        return cols;
    }

    @Override
    protected void initServiceResource() {
        id = (String) getRequest().getAttributes().get("id");
        rev = (String) getRequest().getAttributes().get("rev");
    }

    @Override
    protected String getServiceTitle() {
        return "Product Revisions";
    }

    @Override
    protected RowIterator resolveTable() throws DAOException {
        RowIterator rows;
        ProductRevisions dao = dao(ProductRevisions.class);
        int pid = Integer.parseInt(id);
        if (rev != null) {
            int revnum = Integer.parseInt(rev);
            rows = dao.readRevision(pid, revnum);
        } else {
            rows = dao.readProductRevisions(pid);
        }
        Products productDao = dao(Products.class);
        Row product = productDao.readProduct(pid).next();
        activeRevision = product.value(Products.REV);
        return rows;
    }
}
