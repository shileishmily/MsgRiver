package com.youguu.river.broker;
import com.youguu.river.common.core.SemaphoreCache;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.core.MessageTaskQueue;
import com.youguu.river.common.core.SendMessageCache;
import com.youguu.river.common.model.MessageDispatchTask;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SendMessageController implements Callable<Void> {

    private volatile boolean stoped = false;

    private AtomicBoolean flushTask = new AtomicBoolean(false);

    private ThreadLocal<ConcurrentLinkedQueue<MessageDispatchTask>> requestCacheList = new ThreadLocal<ConcurrentLinkedQueue<MessageDispatchTask>>() {
        protected ConcurrentLinkedQueue<MessageDispatchTask> initialValue() {
            return new ConcurrentLinkedQueue<MessageDispatchTask>();
        }
    };

    private final Timer timer = new Timer("SendMessageTaskMonitor", true);

    public void stop() {
        stoped = true;
    }

    public boolean isStoped() {
        return stoped;
    }

    public Void call() {
        int period = MessageSystemConfig.SendMessageControllerPeriodTimeValue;
        int commitNumber = MessageSystemConfig.SendMessageControllerTaskCommitValue;
        int sleepTime = MessageSystemConfig.SendMessageControllerTaskSleepTimeValue;

        ConcurrentLinkedQueue<MessageDispatchTask> queue = requestCacheList.get();
        SendMessageCache ref = SendMessageCache.getInstance();

        while (!stoped) {
            SemaphoreCache.acquire(MessageSystemConfig.NotifyTaskSemaphoreValue);
            MessageDispatchTask task = MessageTaskQueue.getInstance().getTask();

            queue.add(task);

            if (queue.size() == 0) {
                try {
                    Thread.sleep(sleepTime);
                    continue;
                } catch (InterruptedException ex) {
                    Logger.getLogger(SendMessageController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (queue.size() > 0 && (queue.size() % commitNumber == 0 || flushTask.get() == true)) {
                ref.commit(queue);
                queue.clear();
                flushTask.compareAndSet(true, false);
            }

            timer.scheduleAtFixedRate(new TimerTask() {

                public void run() {
                    try {
                        flushTask.compareAndSet(false, true);
                    } catch (Exception e) {
                        System.out.println("SendMessageTaskMonitor happen exception");
                    }
                }
            }, 1000 * 1, period);
        }
        return null;
    }
}
