package com.youguu.river.broker.server;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.youguu.river.broker.AckPullMessageController;
import com.youguu.river.broker.AckPushMessageController;
import com.youguu.river.broker.SendMessageController;
import com.youguu.river.common.netty.NettyClustersConfig;

import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;


public class BrokerParallelServer implements RemotingServer {

    protected int parallel = NettyClustersConfig.getWorkerThreads();
    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(parallel));
    protected ExecutorCompletionService<Void> executorService;

    public BrokerParallelServer() {

    }

    @Override
    public void init() {
        executorService = new ExecutorCompletionService<Void>(executor);
    }

    @Override
    public void start() {
        for (int i = 0; i < parallel; i++) {
            executorService.submit(new SendMessageController());
            executorService.submit(new AckPullMessageController());
            executorService.submit(new AckPushMessageController());
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
