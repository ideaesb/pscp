
package pscp.restlet.template;

import pscp.restlet.template.ResourceTemplate;
import freemarker.ext.beans.StringModel;
import freemarker.template.Configuration;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.SimpleScalar;
import freemarker.template.SimpleSequence;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.TemplateModelIterator;
import freemarker.template.TemplateSequenceModel;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Date;
import java.util.LinkedList;
import java.util.UUID;
import junit.framework.TestCase;
import org.restlet.resource.Representation;

/**
 *
 * @author iws
 */
public class ResourceTemplateTest extends TestCase {
    
    public ResourceTemplateTest(String testName) {
        super(testName);
    }
    
    private Configuration config() {
        Configuration config = new Configuration();
        config.setClassForTemplateLoading(ResourceTemplateTest.class, "");
        return config;
    }

    public void testTestConfiguration() throws IOException {
        config().getTemplate("container.ftl");
    }
    
    private ResourceTemplate buildSub1Template() {
        SimpleSequence seq = new SimpleSequence();
        seq.add("xxx");
        seq.add(UUID.randomUUID());
        seq.add(22.5);
        return ResourceTemplate.get("sub1.ftl", seq);
    }

    private ResourceTemplate buildSub2Template() {
        SimpleHash seq = new SimpleHash();
        seq.put("now",new Date().toString());
        seq.put("id",UUID.randomUUID());
        seq.put("value",22.5);
        return ResourceTemplate.get("sub2.ftl", seq);
    }

    public void testHashHasValue() throws TemplateModelException {
        SimpleHash h = new SimpleHash();
        TemplateModel v = h.get("a");
        assertNull(v);
    }

    public void testSimple() throws Exception {
        SimpleHash pageModel = new SimpleHash();
        pageModel.put("title","Yee haw");
        ResourceTemplate page = ResourceTemplate.get("container.ftl", pageModel);
        page.addModel("aux", new SimpleScalar("foo"));
        page.addTemplate(buildSub1Template());
        page.addTemplate(buildSub2Template());
        ResourceTemplate.dumpModel(page.buildModel(),System.out,false);
        Representation rep = page.render(config());
        System.out.println(rep.getText());
    }

    public void testSimpleWrappers() throws Exception {
        SimpleHash hash = new SimpleHash();
        hash.setObjectWrapper(ObjectWrapper.DEFAULT_WRAPPER);
        SimpleHash sub = new SimpleHash();
        sub.put("foo","bar");
        sub.put("1","2");
        SimpleSequence list = new SimpleSequence();
        list.add("one");
        list.add("two");
        hash.put("sub",sub);
        hash.put("list",list);
        hash.put("text","text");
        hash.put("uuid",UUID.randomUUID());

        ResourceTemplate.dumpModel(hash,System.out,false);
    }


}
