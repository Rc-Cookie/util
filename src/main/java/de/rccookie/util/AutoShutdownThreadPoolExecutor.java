//package de.rccookie.util;
//
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.RejectedExecutionHandler;
//import java.util.concurrent.ThreadFactory;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import org.jetbrains.annotations.NotNull;
//
//public class AutoShutdownThreadPoolExecutor extends ThreadPoolExecutor {
//
//    private static final Set<AutoShutdownThreadPoolExecutor> EXECUTORS = new HashSet<>();
//    static {
//        Thread managerThread = new Thread(AutoShutdownThreadPoolExecutor::runManager, "AutoShutdown");
//        managerThread.setPriority(Thread.MIN_PRIORITY);
//        managerThread.start();
//    }
//
//    public AutoShutdownThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue) {
//        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, r -> factory(r, Thread::new));
//        register();
//    }
//
//    public AutoShutdownThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory) {
//        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, r -> factory(r, threadFactory));
//        register();
//    }
//
//    public AutoShutdownThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull RejectedExecutionHandler handler) {
//        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, r -> factory(r, Thread::new), handler);
//        register();
//    }
//
//    public AutoShutdownThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, @NotNull ThreadFactory threadFactory, @NotNull RejectedExecutionHandler handler) {
//        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, r -> factory(r, threadFactory), handler);
//        register();
//    }
//
//    private void register() {
//        synchronized(EXECUTORS) {
//            EXECUTORS.add(this);
//        }
//    }
//
//    private static Thread factory(Runnable r, ThreadFactory factory) {
//        Thread t = factory.newThread(r);
//        t.setDaemon(true);
//        return t;
//    }
//
//    private static void runManager() {
//        while(!finished()) try {
//            //noinspection BusyWait
//            Thread.sleep(1000);
//        } catch(InterruptedException e) {
//            Console.warn(e);
//        }
//    }
//
//    private static boolean finished() {
//        synchronized(EXECUTORS) {
//            for(ThreadPoolExecutor executor : EXECUTORS)
//                if(!executor.isShutdown() && executor.getActiveCount() != 0) return false;
//        }
//        Thread current = Thread.currentThread();
//        for(Thread t : Thread.getAllStackTraces().keySet()) {
//            if(t == current) continue;
//            if(!t.isAlive()) continue;
//            if(t.isDaemon()) continue;
//            if(t.getName().equals("DestroyJavaVM")) continue;
//            return false;
//        }
//        return true;
//    }
//}
