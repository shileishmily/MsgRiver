package com.youguu.river.common.context;

import com.youguu.river.common.model.RemoteChannelData;
import com.youguu.river.common.model.SubscriptionData;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.iterators.FilterIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class ConsumerContext {

    private static final CopyOnWriteArrayList<ClustersRelation> relationArray = new CopyOnWriteArrayList<ClustersRelation>();
    private static final CopyOnWriteArrayList<ClustersState> stateArray = new CopyOnWriteArrayList<ClustersState>();

    public static void setClustersStat(String clusters, int stat) {
        stateArray.add(new ClustersState(clusters, stat));
    }

    public static int getClustersStat(String clusters) {

        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                String clustersId = ((ClustersState) object).getClusters();
                return clustersId.compareTo(clusters) == 0;
            }
        };

        Iterator iterator = new FilterIterator(stateArray.iterator(), predicate);

        ClustersState state = null;
        while (iterator.hasNext()) {
            state = (ClustersState) iterator.next();
            break;

        }
        return (state != null) ? state.getState() : 0;
    }

    public static ConsumerClusters selectByClusters(String clusters) {
        Predicate predicate = new Predicate() {
            public boolean evaluate(Object object) {
                String id = ((ClustersRelation) object).getId();
                return id.compareTo(clusters) == 0;
            }
        };

        Iterator iterator = new FilterIterator(relationArray.iterator(), predicate);

        ClustersRelation relation = null;
        while (iterator.hasNext()) {
            relation = (ClustersRelation) iterator.next();
            break;
        }

        return (relation != null) ? relation.getClusters() : null;
    }

    public static List<ConsumerClusters> selectByTopic(String topic) {

        List<ConsumerClusters> clusters = new ArrayList<ConsumerClusters>();

        for (int i = 0; i < relationArray.size(); i++) {
            ConcurrentHashMap<String, SubscriptionData> subscriptionTable = relationArray.get(i).getClusters().getSubMap();
            if (subscriptionTable.containsKey(topic)) {
                clusters.add(relationArray.get(i).getClusters());
            }
        }

        return clusters;
    }

    public static void addClusters(String clusters, RemoteChannelData channelinfo) {
        ConsumerClusters manage = selectByClusters(clusters);
        if (manage == null) {
            ConsumerClusters newClusters = new ConsumerClusters(clusters);
            newClusters.attachRemoteChannelData(channelinfo.getClientId(), channelinfo);
            relationArray.add(new ClustersRelation(clusters, newClusters));
        } else if (manage.findRemoteChannelData(channelinfo.getClientId()) != null) {
            manage.detachRemoteChannelData(channelinfo.getClientId());
            manage.attachRemoteChannelData(channelinfo.getClientId(), channelinfo);
        } else {
            String topic = channelinfo.getSubcript().getTopic();
            boolean touchChannel = manage.getSubMap().containsKey(topic);
            if (touchChannel) {
                manage.attachRemoteChannelData(channelinfo.getClientId(), channelinfo);
            } else {
                manage.getSubMap().clear();
                manage.getChannelMap().clear();
                manage.attachRemoteChannelData(channelinfo.getClientId(), channelinfo);
            }
        }
    }

    public static void unLoad(String clientId) {

        for (int i = 0; i < relationArray.size(); i++) {
            String id = relationArray.get(i).getId();
            ConsumerClusters manage = relationArray.get(i).getClusters();

            if (manage.findRemoteChannelData(clientId) != null) {
                manage.detachRemoteChannelData(clientId);
            }

            if (manage.getChannelMap().size() == 0) {
                ClustersRelation relation = new ClustersRelation();
                relation.setId(id);
                relationArray.remove(id);
            }
        }
    }
}
