package com.youguu.consumer.test;


import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.msg.Message;
import com.youguu.river.consumer.AvatarMQConsumer;
import com.youguu.river.consumer.ProducerMessageHook;

public class ConsumerOneByQuant {

    private static ProducerMessageHook hook = new ProducerMessageHook() {
        public ConsumerAckMessage hookMessage(Message message) {
            System.out.printf("ConsumerOneByQuant 收到消息编号:%s,消息内容:%s\n", message.getMsgId(), new String(message.getBody()));
            ConsumerAckMessage result = new ConsumerAckMessage();
            result.setStatus(ConsumerAckMessage.SUCCESS);
            return result;
        }
    };

    public static void main(String[] args) {
        AvatarMQConsumer consumer = new AvatarMQConsumer("127.0.0.1:18888", "test_topic", hook);
        consumer.init();
        consumer.setClusterId("aaaaaab");
        consumer.receiveMode();
        consumer.start();
    }
}
