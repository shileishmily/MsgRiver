package com.youguu.river.common.context;

import com.youguu.river.common.model.RemoteChannelData;
import com.youguu.river.common.model.SubscriptionData;
import com.youguu.river.common.netty.NettyUtil;
import io.netty.channel.Channel;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.Predicate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConsumerClusters {

    private int next = 0;
    private final String clustersId;
    private final ConcurrentHashMap<String, SubscriptionData> subMap
            = new ConcurrentHashMap<String, SubscriptionData>();

    private final ConcurrentHashMap<String, RemoteChannelData> channelMap
            = new ConcurrentHashMap<String, RemoteChannelData>();

    private final List<RemoteChannelData> channelList = Collections.synchronizedList(new ArrayList<RemoteChannelData>());

    public ConsumerClusters(String clustersId) {
        this.clustersId = clustersId;
    }

    public String getClustersId() {
        return clustersId;
    }

    public ConcurrentHashMap<String, SubscriptionData> getSubMap() {
        return subMap;
    }

    public ConcurrentHashMap<String, RemoteChannelData> getChannelMap() {
        return channelMap;
    }

    public void attachRemoteChannelData(String clientId, RemoteChannelData channelinfo) {
        if (findRemoteChannelData(channelinfo.getClientId()) == null) {
            channelMap.put(clientId, channelinfo);
            subMap.put(channelinfo.getSubcript().getTopic(), channelinfo.getSubcript());
            channelList.add(channelinfo);
        } else {
            System.out.println("consumer clusters exists! it's clientId:" + clientId);
        }
    }

    public void detachRemoteChannelData(String clientId) {
        channelMap.remove(clientId);

        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                String id = ((RemoteChannelData) object).getClientId();
                return id.compareTo(clientId) == 0;
            }
        };

        RemoteChannelData data = (RemoteChannelData) CollectionUtils.find(channelList, predicate);
        if (data != null) {
            channelList.remove(data);
        }
    }

    public RemoteChannelData findRemoteChannelData(String clientId) {
        return (RemoteChannelData) MapUtils.getObject(channelMap, clientId);
    }

    public RemoteChannelData nextRemoteChannelData() {

        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                RemoteChannelData data = (RemoteChannelData) object;
                Channel channel = data.getChannel();
                return NettyUtil.validateChannel(channel);
            }
        };

        CollectionUtils.filter(channelList, predicate);
        return channelList.get(next++ % channelList.size());
    }

    public SubscriptionData findSubscriptionData(String topic) {
        return this.subMap.get(topic);
    }
}
