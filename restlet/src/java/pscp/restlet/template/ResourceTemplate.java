
package pscp.restlet.template;

import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.restlet.data.MediaType;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;

/**
 *
 * @author iws
 */
public abstract class ResourceTemplate {
    protected String templateName;
    protected String templateFileName;
    private List<ResourceTemplate> children = new ArrayList<ResourceTemplate>();
    private Map<String,TemplateModel> models = new HashMap<String,TemplateModel>();

    public static ResourceTemplate get(String template,final TemplateModel model) {
        return get(template,template.substring(0,template.lastIndexOf('.')),model);
    }
    public static ResourceTemplate get(String template,String name,final TemplateModel model) {
        ResourceTemplate rt = new ResourceTemplate() {

            @Override
            protected TemplateModel buildResourceModel() {
                return model;
            }

        };
        rt.templateFileName = template;
        rt.templateName = name;
        return rt;
    }

    public String getTemplateName() {
        return templateName;
    }
    
    public void addTemplate(ResourceTemplate child) {
        children.add(child);
    }
    
    public void addModel(String name,TemplateModel model) {
        models.put(name,model);
    }
    
    protected abstract TemplateModel buildResourceModel();
    
    public TemplateModel buildModel() {
        SimpleHash model = new SimpleHash();
        for (String subModel : models.keySet()) {
            checkModel(model,subModel);
            model.put(subModel, models.get(subModel));
        }
        for (ResourceTemplate child : children) {
            checkModel(model,child.getTemplateName());
            model.put(child.getTemplateName(),child.buildResourceModel());
        }
        model.put(getTemplateName(),buildResourceModel());
        return model;
    }
    
    public Representation render(Configuration config) throws TemplateException, IOException {
        Template template = config.getTemplate(templateFileName);
        StringWriter buf = new StringWriter();
        template.process(buildModel(), buf);
        return new StringRepresentation(buf.getBuffer(), MediaType.TEXT_HTML);
    }

    public static void dumpModel(TemplateModel model,PrintStream out,boolean showClasses) throws TemplateModelException {
        showClasses = true;
        class Entry {
            String name;
            TemplateModel model;
            int indent;
            Entry(String name,TemplateModel model,int indent) {
                this.name = name;
                this.model = model;
                this.indent = indent;
            }
        }
        LinkedList<Entry> stack = new LinkedList<Entry>();
        stack.add(new Entry("[root]",model,0));
        while (!stack.isEmpty()) {
            Entry next = stack.remove();
            for (int i = 0; i < next.indent; i++) {
                out.append('\t');
            }
            out.print(next.name);
            out.append(' ');
            if (next.model instanceof StringModel) {
                out.print("'" + next.model.toString() + "'");
            } else if (next.model instanceof TemplateSequenceModel) {
                TemplateSequenceModel seq = (TemplateSequenceModel) next.model;
                for (int i = seq.size() - 1; i >= 0; i--) {
                    stack.push(new Entry("" + i,seq.get(i),next.indent+1));
                }
            } else if (next.model instanceof TemplateHashModelEx) {
                TemplateHashModelEx hash = (TemplateHashModelEx) next.model;
                TemplateModelIterator keys = hash.keys().iterator();
                while (keys.hasNext()) {
                    TemplateModel key = keys.next();
                    stack.push(new Entry(key.toString(),hash.get(key.toString()),next.indent+1));
                }
            } else {
                if (next.model != null) {
                    out.print(next.model.toString());
                } else {
                    out.print("null");
                }
                if (showClasses) {
                    out.print(" [ " + next.model.getClass().getSimpleName() + " ]");
                }
            }
            out.println();
        }
        out.flush();
    }

    private void checkModel(SimpleHash model, String key) {
        try {
            if (model.get(key) != null) {
                throw new IllegalStateException("model already contains key " + key);
            }
        } catch (TemplateModelException tme) {
            throw new RuntimeException("Unexpected exception checking for existing key",tme);
        }
    }
}
