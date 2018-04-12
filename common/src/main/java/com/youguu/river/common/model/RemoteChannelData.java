package com.youguu.river.common.model;

import io.netty.channel.Channel;
import org.apache.commons.lang3.builder.EqualsBuilder;

public class RemoteChannelData {

    private Channel channel;
    private String clientId;

    private SubscriptionData subcript;

    public SubscriptionData getSubcript() {
        return subcript;
    }

    public void setSubcript(SubscriptionData subcript) {
        this.subcript = subcript;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getClientId() {
        return clientId;
    }

    public RemoteChannelData(Channel channel, String clientId) {
        this.channel = channel;
        this.clientId = clientId;
    }

    public boolean equals(Object obj) {
        boolean result = false;
        if (obj != null && RemoteChannelData.class.isAssignableFrom(obj.getClass())) {
            RemoteChannelData info = (RemoteChannelData) obj;
            result = new EqualsBuilder().append(clientId, info.getClientId())
                    .isEquals();
        }
        return result;
    }

}
