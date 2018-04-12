package com.youguu.river.broker;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import com.youguu.river.common.msg.Message;
import com.youguu.river.common.msg.ProducerAckMessage;
import com.youguu.river.common.core.SemaphoreCache;
import com.youguu.river.common.context.ConsumerClusters;
import com.youguu.river.common.context.ConsumerContext;
import com.youguu.river.common.core.AckMessageCache;
import com.youguu.river.common.core.AckTaskQueue;
import com.youguu.river.common.core.MessageTaskQueue;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.model.MessageDispatchTask;
import com.youguu.river.common.core.ChannelCache;
import org.apache.commons.collections.Closure;
import org.apache.commons.collections.Predicate;
import org.apache.commons.collections.ClosureUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.functors.AnyPredicate;
import com.google.common.base.Joiner;

public class ProducerMessageHook implements ProducerMessageListener {

    private List<ConsumerClusters> clustersSet = new ArrayList<>();
    private List<ConsumerClusters> focusTopicGroup = null;

    private void filterByTopic(String topic) {
        Predicate focusAllPredicate = new Predicate() {
            public boolean evaluate(Object object) {
                ConsumerClusters clusters = (ConsumerClusters) object;
                return clusters.findSubscriptionData(topic) != null;
            }
        };

        AnyPredicate any = new AnyPredicate(new Predicate[]{focusAllPredicate});

        Closure joinClosure = new Closure() {
            public void execute(Object input) {
                if (input instanceof ConsumerClusters) {
                    ConsumerClusters clusters = (ConsumerClusters) input;
                    clustersSet.add(clusters);
                }
            }
        };

        Closure ignoreClosure = new Closure() {
            public void execute(Object input) {
            }
        };

        Closure ifClosure = ClosureUtils.ifClosure(any, joinClosure, ignoreClosure);

        CollectionUtils.forAllDo(focusTopicGroup, ifClosure);
    }

    private boolean checkClustersSet(Message msg, String requestId) {
        if (clustersSet.size() == 0) {
            System.out.println("AvatarMQ don't have match clusters!");
            ProducerAckMessage ack = new ProducerAckMessage();
            ack.setMsgId(msg.getMsgId());
            ack.setAck(requestId);
            ack.setStatus(ProducerAckMessage.SUCCESS);
            AckTaskQueue.pushAck(ack);
            SemaphoreCache.release(MessageSystemConfig.AckTaskSemaphoreValue);
            return false;
        } else {
            return true;
        }
    }

    private void dispatchTask(Message msg, String topic) {
        List<MessageDispatchTask> tasks = new ArrayList<MessageDispatchTask>();

        for (int i = 0; i < clustersSet.size(); i++) {
            MessageDispatchTask task = new MessageDispatchTask();
            task.setClusters(clustersSet.get(i).getClustersId());
            task.setTopic(topic);
            task.setMessage(msg);
            tasks.add(task);

        }

        MessageTaskQueue.getInstance().pushTask(tasks);

        for (int i = 0; i < tasks.size(); i++) {
            SemaphoreCache.release(MessageSystemConfig.NotifyTaskSemaphoreValue);
        }
    }

    private void taskAck(Message msg, String requestId) {
        try {
            Joiner joiner = Joiner.on(MessageSystemConfig.MessageDelimiter).skipNulls();
            String key = joiner.join(requestId, msg.getMsgId());
            AckMessageCache.getAckMessageCache().appendMessage(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void hookProducerMessage(Message msg, String requestId, Channel channel) {

        ChannelCache.pushRequest(requestId, channel);

        String topic = msg.getTopic();

        focusTopicGroup = ConsumerContext.selectByTopic(topic);

        filterByTopic(topic);

        if (checkClustersSet(msg, requestId)) {
            dispatchTask(msg, topic);
            taskAck(msg, requestId);
            clustersSet.clear();
        } else {
            return;
        }
    }
}
