package com.youguu.river.common.core;

import com.youguu.river.common.model.MessageDispatchTask;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class SendMessageCache extends MessageCache<MessageDispatchTask> {

    private Phaser phaser = new Phaser(0);

    private SendMessageCache() {

    }

    private static SendMessageCache cache;

    public synchronized static SendMessageCache getInstance() {
        if (cache == null) {
            cache = new SendMessageCache();
        }
        return cache;
    }

    public void parallelDispatch(LinkedList<MessageDispatchTask> list) {
        List<Callable<Void>> tasks = new ArrayList<Callable<Void>>();
        int startPosition = 0;
        Pair<Integer, Integer> pair = calculateBlocks(list.size(), list.size());
        int numberOfThreads = pair.getRight();
        int blocks = pair.getLeft();

        for (int i = 0; i < numberOfThreads; i++) {
            MessageDispatchTask[] task = new MessageDispatchTask[blocks];
            phaser.register();
            System.arraycopy(list.toArray(), startPosition, task, 0, blocks);
            tasks.add(new SendMessageTask(phaser, task));
            startPosition += blocks;
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        for (Callable<Void> element : tasks) {
            executor.submit(element);
        }
    }
}
