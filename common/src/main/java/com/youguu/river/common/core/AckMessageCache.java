package com.youguu.river.common.core;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AckMessageCache extends MessageCache<String> {

    private CyclicBarrier barrier = null;
    private long succTaskCount = 0;

    private AckMessageCache() {

    }

    public long getSuccTaskCount() {
        return succTaskCount;
    }

    private static class AckMessageCacheHolder {

        public static AckMessageCache cache = new AckMessageCache();
    }

    public static AckMessageCache getAckMessageCache() {
        return AckMessageCacheHolder.cache;
    }

    public void parallelDispatch(LinkedList<String> list) {
        List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();
        List<Future<Long>> futureList = new ArrayList<Future<Long>>();
        int startPosition = 0;
        Pair<Integer, Integer> pair = calculateBlocks(list.size(), list.size());
        int numberOfThreads = pair.getRight();
        int blocks = pair.getLeft();

        barrier = new CyclicBarrier(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            String[] task = new String[blocks];
            System.arraycopy(list.toArray(), startPosition, task, 0, blocks);
            tasks.add(new AckMessageTask(barrier, task));
            startPosition += blocks;
        }

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        try {
            futureList = executor.invokeAll(tasks);
        } catch (InterruptedException ex) {
            Logger.getLogger(AckMessageCache.class.getName()).log(Level.SEVERE, null, ex);
        }

        for (Future<Long> longFuture : futureList) {
            try {
                succTaskCount += longFuture.get();
            } catch (InterruptedException ex) {
                Logger.getLogger(AckMessageCache.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ExecutionException ex) {
                Logger.getLogger(AckMessageCache.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
