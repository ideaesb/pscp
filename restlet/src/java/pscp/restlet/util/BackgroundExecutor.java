package pscp.restlet.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author iws
 */
public class BackgroundExecutor {

    private final ExecutorService executor;
    private final Thread reaper;
    private Logger logger;
    private final LinkedBlockingQueue<Batch> batchQueue;
    private volatile Batch current;

    public BackgroundExecutor() {
        batchQueue = new LinkedBlockingQueue<Batch>();
        executor = new ThreadPoolExecutor(1,1 , 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new ThreadFactory() {

            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setPriority(Thread.MIN_PRIORITY);
                t.setDaemon(false);
                t.setName("background-executor");
                return t;
            }
        });
        reaper = new Thread(new Runnable() {

            public void run() {
                while (true) {
                    try {
                        reap();
                    } catch (Exception ex) {
                        logger().log(Level.SEVERE, "Unhandled reaping exception", ex);
                    }
                }
            }
        });
        reaper.setDaemon(false);
        reaper.setName("background-executor-reaper");
        reaper.setPriority(Thread.MIN_PRIORITY);
        reaper.start();
    }

    private Logger logger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    private void reap() throws InterruptedException {
        current = batchQueue.take();
        logger().info("reaping " + current.getName() + " : " + current.getJobsRemaining());
        current.finish();
        current = null;
    }

    public List<Batch> listBatches() {
        List<Batch> batches = new ArrayList<Batch>();
        if (current != null) {
            batches.add(current);
        }
        batches.addAll(batchQueue);
        return batches;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void submitJobs(String title, List<Runnable> jobs, Runnable complete) {
        submitJobs(new BatchDefinition(title, jobs, complete));
    }
    
    public void submitJobs(BatchDefinition def) {
        Batch batch = new Batch(def,executor);
        batchQueue.add(batch);
    }

    public static class BatchDefinition {
        private final String title;
        private final List<Runnable> jobs;
        private final Runnable complete;

        public BatchDefinition(String title,
                List<Runnable> jobs, Runnable complete) {
            this.title = title;
            this.jobs = jobs;
            this.complete = complete;
        }

        public int getNumberOfJobs() {
            return jobs.size();
        }

    }

    public class Batch {
        private final BatchDefinition def;
        private final CompletionService completer;
        private final List<Future> futures;
        private volatile int jobsRemaining;
        private volatile boolean running;

        private Batch(BatchDefinition def,Executor executor) {
            this.def = def;
            completer = new ExecutorCompletionService(executor);
            jobsRemaining = def.jobs.size();
            futures = new ArrayList<Future>(jobsRemaining);
            for (Runnable r: def.jobs) {
                futures.add(completer.submit(r,null));
            }
            logger().info("scheduled " + futures.size() + " jobs");
            running = true;
        }

        public void cancel() {
            running = false;
            logger().info("canceling " + futures.size() + " jobs");
            if (current == this) {
                current = null;
            }
            batchQueue.remove(this);
            for (Future f: futures) {
                f.cancel(true);
            }
            // for real, generic use, the callback should support
            // varying conditions (success, cancel, failure, etc.)
            if (def.complete != null) {
                def.complete.run();
            }
        }

        public int getJobsRemaining() {
            return jobsRemaining;
        }

        public String getName() {
            return def.title;
        }

        private void finish() {
            logger().info("finish job processing " + jobsRemaining);
            while (jobsRemaining-- > 0 && running) {
                try {
                    Future f = completer.take();
                    f.get();
                } catch (InterruptedException ex) {
                    logger().info("Interrupted!");
                    break;
                } catch (ExecutionException ex) {
                    logger().log(Level.SEVERE,"Error",ex.getCause());
                } catch (CancellationException ce) {
                    logger().log(Level.INFO,"BackgroundExecutor batch job found to be cancelled");
                }
            }
            if (!running) {
                logger().info("batch exited because not running");
            }
            if (def.complete != null && running) {
                def.complete.run();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        BackgroundExecutor bg = new BackgroundExecutor();
        for (int i = 0; i < 3; i++) {
            List<Runnable> jobs = new ArrayList<Runnable>();
                final int ii = i;
            for (int j = 0; j < 10; j++) {
                final int jj = j;
                jobs.add(new Runnable() {

                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(BackgroundExecutor.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        System.out.println("job " + jj + " of batch " + ii + " done");
                    }
                });
            }
            bg.submitJobs("batch" + i, jobs , new Runnable() {

                public void run() {
                    System.out.println("batch " + ii + " all done");
                    if (ii == 2) {
                        System.exit(0);
                    }
                }
            });
            bg.listBatches();
        }
    }
}
