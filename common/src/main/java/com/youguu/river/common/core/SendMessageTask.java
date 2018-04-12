package com.youguu.river.common.core;

import com.youguu.river.common.context.ClustersState;
import com.youguu.river.common.context.ConsumerContext;
import com.youguu.river.common.launcher.SendMessageLauncher;
import com.youguu.river.common.model.*;
import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.msg.Message;
import com.youguu.river.common.netty.NettyUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.Phaser;

public class SendMessageTask implements Callable<Void> {

    private MessageDispatchTask[] tasks;
    private Phaser phaser = null;
    private SendMessageLauncher launcher = SendMessageLauncher.getInstance();

    public SendMessageTask(Phaser phaser, MessageDispatchTask[] tasks) {
        this.phaser = phaser;
        this.tasks = tasks;
    }

    public Void call() throws Exception {
        for (MessageDispatchTask task : tasks) {
            Message msg = task.getMessage();

            if (ConsumerContext.selectByClusters(task.getClusters()) != null) {
                RemoteChannelData channel = ConsumerContext.selectByClusters(task.getClusters()).nextRemoteChannelData();

                ResponseMessage response = new ResponseMessage();
                response.setMsgSource(MessageSource.AvatarMQBroker);
                response.setMsgType(MessageType.AvatarMQMessage);
                response.setMsgParams(msg);
                response.setMsgId(new MessageIdGenerator().generate());

                try {
                    if (!NettyUtil.validateChannel(channel.getChannel())) {
                        ConsumerContext.setClustersStat(task.getClusters(), ClustersState.NETWORKERR);
                        continue;
                    }

                    RequestMessage request = (RequestMessage) launcher.launcher(channel.getChannel(), response);

                    ConsumerAckMessage result = (ConsumerAckMessage) request.getMsgParams();

                    if (result.getStatus() == ConsumerAckMessage.SUCCESS) {
                        ConsumerContext.setClustersStat(task.getClusters(), ClustersState.SUCCESS);
                    }
                } catch (Exception e) {
                    ConsumerContext.setClustersStat(task.getClusters(), ClustersState.ERROR);
                }
            }
        }
        phaser.arriveAndAwaitAdvance();
        return null;
    }
}
