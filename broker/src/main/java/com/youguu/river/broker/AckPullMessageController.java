package com.youguu.river.broker;

import com.youguu.river.common.msg.ProducerAckMessage;
import com.youguu.river.common.core.AckTaskQueue;
import com.youguu.river.common.core.ChannelCache;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.core.SemaphoreCache;
import com.youguu.river.common.model.MessageType;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.model.MessageSource;
import com.youguu.river.common.netty.NettyUtil;
import io.netty.channel.Channel;
import java.util.concurrent.Callable;

public class AckPullMessageController implements Callable<Void> {

    private volatile boolean stoped = false;

    public void stop() {
        stoped = true;
    }

    public boolean isStoped() {
        return stoped;
    }

    public Void call() {
        while (!stoped) {
            SemaphoreCache.acquire(MessageSystemConfig.AckTaskSemaphoreValue);
            ProducerAckMessage ack = AckTaskQueue.getAck();
            String requestId = ack.getAck();
            ack.setAck("");

            Channel channel = ChannelCache.findChannel(requestId);
            if (NettyUtil.validateChannel(channel)) {
                ResponseMessage response = new ResponseMessage();
                response.setMsgId(requestId);
                response.setMsgSource(MessageSource.AvatarMQBroker);
                response.setMsgType(MessageType.AvatarMQProducerAck);
                response.setMsgParams(ack);

                channel.writeAndFlush(response);
            }
        }
        return null;
    }
}
