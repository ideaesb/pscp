/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pscp.restlet.util;

import dao.DAOCollection;
import dao.DAOException;
import dao.DAOFactory;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author en
 */
public abstract class Ingester {
    private DAOFactory daoFactory;
    private List<LogRecord> messages;
    private HLogger hlogger;
    private Logger logger;
    private File logFile;
    private DAOCollection daos;
    protected boolean dryRun;
    protected File inputFile;

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    protected final DAOCollection daos() {
        return daos;
    }

    protected void reopenDAOs() throws DAOException {
        if (daos != null) {
            daos.close();
        }
        daos = daoFactory.create(true);
    }

    protected final DAOFactory daoFactory() {
        return daoFactory;
    }

    public void setLogFile(File file) {
        this.logFile = file;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    protected final Logger logger() {
        return logger;
    }

    protected void pushLoggingScope(String msg) {
        hlogger.push(new LogRecord(Level.INFO, msg));
        logger.finest(msg);
    }

    protected void popLoggingScope() {
        hlogger.pop();
    }

    protected final void warn(String string) {
        logger.warning(string);
        if (hlogger != null) {
            hlogger.log(new LogRecord(Level.WARNING, string));
        }
    }

    protected final void warn(String string,Object ... args) {
        String record = String.format(string, args);
        logger.warning(record);
        if (hlogger != null) {
            hlogger.log(new LogRecord(Level.WARNING, record));
        }
    }

    protected final void warnex(String string, Throwable t) {
        logger.log(Level.WARNING,string,t);
        LogRecord r = new LogRecord(Level.WARNING, string);
        r.setThrown(t);
        if (hlogger != null) {
            hlogger.log(r);
        }
    }
    
    protected final void err(String msg) {
        logger.log(Level.SEVERE,msg);
        if (hlogger != null) {
            hlogger.log(new LogRecord(Level.SEVERE, msg));
        }
    }

    protected final void info(String string, Object... args) {
        String record = String.format(string, args);
        logger.info(record);
        if (hlogger != null) {
            hlogger.log(new LogRecord(Level.INFO, record));
        }
    }
    
    public final void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    public final void commit() throws DAOException {
        logger.info("commit");
        daos.commit();
    }

    protected abstract File getLogFile();

    public final List<LogRecord> ingest() throws IOException, DAOException {
        hlogger = new HLogger();
        daos = daoFactory.create(true);
        messages = new ArrayList<LogRecord>();
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        File logFile = this.logFile == null ? getLogFile() : this.logFile;
        Formatter fmt = new Formatter() {
            final String NL = File.pathSeparatorChar == ':' ? "\n" : "\r\n";
            @Override
            public String format(LogRecord record) {
                return record.getLevel() + ":" + record.getMessage() + NL;
            }
        };
        logger.setLevel(Level.ALL);
        FileOutputLoggingHandler loggingHandler = FileOutputLoggingHandler.attach(logger, logFile, fmt, Level.ALL);
        try {
            doIngest();
        } catch (IOException ioe) {
            rollback();
            throw ioe;
        } catch (DAOException dao) {
            rollback();
            throw dao;
        } finally {
            messages.add(hlogger.finish());
            hlogger = null;
            loggingHandler.detach();
        }
        return messages;
    }

    protected abstract void doIngest() throws IOException, DAOException;

    public final void rollback() throws DAOException {
        logger.info("rollback");
        daos.rollback();
    }

    public final void setDaoFactory(DAOFactory daoFactory) {
        this.daoFactory = daoFactory;
    }
}
