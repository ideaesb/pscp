/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pscp.restlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author en
 */
public class HLogger {
    private Stack<LogRecord> stack = new Stack<LogRecord>();
    private Stack<List<LogRecord>> children = new Stack<List<LogRecord>>();
    private LogRecord root;

    public void push(LogRecord scope) {
        if (root == null) {
            root = scope;
        }
        if (stack.size() > 0) {
            log(scope);
        }
        stack.push(scope);
        children.push(null);
    }

    public void log(LogRecord r) {
        List<LogRecord> ch = children.peek();
        if (ch == null) {
            children.pop();
            children.push(ch = new ArrayList<LogRecord>());
        }
        ch.add(r);
    }

    public LogRecord pop() {
        LogRecord context = null;
        if (!stack.empty()) {
            context = popRecord();
        }
        return context;
    }

    private LogRecord popRecord() {
        LogRecord r = stack.pop();
        List<LogRecord> ch = children.pop();
        if (ch != null) {
            r.setParameters(ch.toArray(new Object[ch.size()]));
        }
        Level max = findMax(r);
        if (max != null && max.intValue() > r.getLevel().intValue()) {
            r.setLevel(max);
        }
        return r;
    }

    private void print(List<LogRecord> l) {
        for(LogRecord r: l) System.out.println(r.getMessage());
    }

    private Level findMax(LogRecord r) {
        Level max = Level.FINEST;
        Object[] params = r.getParameters();
        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                if (params[i] instanceof LogRecord) {
                    LogRecord r2 = (LogRecord) params[i];
                    if (r2.getLevel().intValue() > max.intValue()) {
                        max = r2.getLevel();
                    }
                }
            }
        }
        return max;
    }

    public LogRecord finish() {
        while (!stack.empty()) {
            pop();
        }
        return root;
    }

    public static void dump(List<LogRecord> messages) {
        final Formatter fmt = new Formatter() {

            @Override
            public String format(LogRecord arg0) {
                return arg0.getLevel() + " : " + arg0.getMessage() + '\n';
            }
        };
        for (int i = 0; i < messages.size(); i++) {
            HLogger.visit(messages.get(i), new HLogger.RecordVisitor() {

                int indent = 0;
                CharSequence prefix = "";

                public void enter(LogRecord r) {
                    visit(r);
                    indent++;
                    StringBuilder b = new StringBuilder();
                    for (int j = 0; j < indent; j++) {
                        b.append('\t');
                    }
                    prefix = b.toString();
                }

                public void visit(LogRecord r) {
                    System.out.print(prefix);
                    System.out.print(fmt.format(r));
                }

                public void exit() {
                    if (prefix.length() > 2) {
                        prefix = prefix.subSequence(0, prefix.length() - 2);
                    } else {
                        prefix = " ";
                    }
                    indent--;
                }
            });
        }
    }

    public static void visit(LogRecord r, RecordVisitor v) {
        Stack<LogRecord> context = new Stack<LogRecord>();
        context.push(r);
        while (context.size() > 0) {
            r = context.pop();
            if (r == null) {
                v.exit();
            } else {
                Object[] params = r.getParameters();
                if (params != null) {
                    v.enter(r);
                    context.push(null);
                    for (int i = params.length -1; i >= 0; i--) {
                        Object object = params[i];
                        if (object instanceof LogRecord) {
                            context.push((LogRecord) object);
                        }
                    }
                } else {
                    v.visit(r);
                }
            }
        }
    }

    public static interface RecordVisitor {
        void enter(LogRecord r);

        void visit(LogRecord r);

        void exit();
    }

    public static void main(String[] args) throws Exception {
        RecordVisitor v = new RecordVisitor() {

            public void enter(LogRecord r) {
                System.out.println("enter " + r.getMessage());
            }

            public void visit(LogRecord r) {
                System.out.println("visit " + r.getMessage());
            }

            public void exit() {
                System.out.println("exit");
            }

        };
        HLogger h = new HLogger();
        h.push(new LogRecord(Level.INFO,"1"));
        h.log(new LogRecord(Level.INFO,"a"));
        visit(h.finish(), v);
        h = new HLogger();
        h.push(new LogRecord(Level.INFO,"1"));
        h.log(new LogRecord(Level.INFO,"1a"));
        h.push(new LogRecord(Level.INFO,"2"));
        h.log(new LogRecord(Level.INFO,"2a"));
        h.push(new LogRecord(Level.INFO,"3"));
        h.pop();
        h.log(new LogRecord(Level.INFO,"2b"));
        h.log(new LogRecord(Level.INFO,"2c"));
        h.pop();
        h.log(new LogRecord(Level.INFO,"1b"));

        visit(h.finish(),v);
    }
}
