package com.youguu.river.broker;
import com.youguu.river.common.context.ConsumerContext;
import com.youguu.river.common.model.RemoteChannelData;
import com.youguu.river.common.msg.SubscribeMessage;
import com.youguu.river.common.model.SubscriptionData;

public class ConsumerMessageHook implements ConsumerMessageListener {

    public ConsumerMessageHook() {

    }

    public void hookConsumerMessage(SubscribeMessage request, RemoteChannelData channel) {

        System.out.println("receive subcript info groupid:" + request.getClusterId() + " topic:" + request.getTopic() + " clientId:" + channel.getClientId());

        SubscriptionData subscript = new SubscriptionData();

        subscript.setTopic(request.getTopic());
        channel.setSubcript(subscript);

        ConsumerContext.addClusters(request.getClusterId(), channel);
    }
}
