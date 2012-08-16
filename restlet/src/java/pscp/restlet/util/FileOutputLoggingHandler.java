
package pscp.restlet.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 *
 * @author iws
 */
public class FileOutputLoggingHandler {

    public static FileOutputLoggingHandler attach(Logger logger,File dest,Formatter fmt,Level level) throws IOException {
        dest.getAbsoluteFile().getParentFile().mkdirs();
        FileHandler fh = new FileHandler(dest.getAbsolutePath(),0,1);
        fh.setLevel(level);
        fh.setFormatter(fmt);
        FileOutputLoggingHandler handler = new FileOutputLoggingHandler(logger,fh);
        handler.handler.setLevel(level);
        logger.addHandler(handler.handler);
        return handler;
    }
    private final Logger logger;
    private final Handler handler;

    private FileOutputLoggingHandler(Logger logger, FileHandler handler) {
        this.logger = logger;
        this.handler = new ThreadFilterHandler(Thread.currentThread(),handler);
    }

    static class ThreadFilterHandler extends Handler {
        private final Thread thread;
        private final Handler handler;
        public ThreadFilterHandler(Thread thread,Handler handler) {
            this.thread = thread;
            this.handler = handler;
        }
        @Override
        public void publish(LogRecord record) {
            if (Thread.currentThread() == thread) {
                handler.publish(record);
            }
        }

        @Override
        public void flush() {
            handler.flush();
        }

        @Override
        public void close() throws SecurityException {
            handler.close();
        }

    }

    public void detach() {
        logger.removeHandler(handler);
        handler.flush();
        handler.close();
    }

    @Override
    protected void finalize() throws Throwable {
        detach();
    }


}
