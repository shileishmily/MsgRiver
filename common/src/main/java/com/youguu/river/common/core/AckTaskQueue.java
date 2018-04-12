package com.youguu.river.common.core;


import com.youguu.river.common.msg.ProducerAckMessage;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AckTaskQueue {

    private static ConcurrentLinkedQueue<ProducerAckMessage> ackQueue = new ConcurrentLinkedQueue<ProducerAckMessage>();

    public static boolean pushAck(ProducerAckMessage ack) {
        return ackQueue.offer(ack);
    }

    public static boolean pushAck(List<ProducerAckMessage> acks) {
        boolean flag = false;
        for (ProducerAckMessage ack : acks) {
            flag = ackQueue.offer(ack);
        }
        return flag;
    }

    public static ProducerAckMessage getAck() {
        return ackQueue.poll();
    }
}
