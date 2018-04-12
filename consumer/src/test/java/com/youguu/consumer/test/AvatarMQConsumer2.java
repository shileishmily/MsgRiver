package com.youguu.consumer.test;


import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.msg.Message;
import com.youguu.river.consumer.AvatarMQConsumer;
import com.youguu.river.consumer.ProducerMessageHook;

public class AvatarMQConsumer2 {

    private static ProducerMessageHook hook = new ProducerMessageHook() {
        public ConsumerAckMessage hookMessage(Message message) {
            System.out.printf("AvatarMQConsumer2 收到消息编号:%s,消息内容:%s\n", message.getMsgId(), new String(message.getBody()));
            ConsumerAckMessage result = new ConsumerAckMessage();
            result.setStatus(ConsumerAckMessage.SUCCESS);
            return result;
        }
    };

    public static void main(String[] args) {
        AvatarMQConsumer consumer = new AvatarMQConsumer("127.0.0.1:18888", "AvatarMQ-Topic-2", hook);
        consumer.init();
        consumer.setClusterId("AvatarMQCluster2");
        consumer.receiveMode();
        consumer.start();
    }
}
