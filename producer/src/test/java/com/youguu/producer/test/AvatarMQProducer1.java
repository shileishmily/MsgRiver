package com.youguu.producer.test;

import com.youguu.river.common.msg.Message;
import com.youguu.river.common.msg.ProducerAckMessage;
import com.youguu.river.producer.AvatarMQProducer;
import org.apache.commons.lang3.StringUtils;

public class AvatarMQProducer1 {

    public static void main(String[] args) throws InterruptedException {
        AvatarMQProducer producer = new AvatarMQProducer("127.0.0.1:18888", "test_topic");
        producer.setClusterId("aaaaaab");
        producer.init();
        producer.start();

        for (int i = 0; i < 10; i++) {
            Message message = new Message();
            String str = "Hello AvatarMQ From Producer1[" + i + "]";
            message.setBody(str.getBytes());
            ProducerAckMessage result = producer.delivery(message);
            if (result.getStatus() == (ProducerAckMessage.SUCCESS)) {
                System.out.printf("AvatarMQProducer1 发送消息编号:%s\n", result.getMsgId());
            } else {
                System.out.println("发送失败：status="+result.getStatus());
            }

            Thread.sleep(1000);
        }

//        producer.shutdown();
        System.out.println(StringUtils.center("AvatarMQProducer1 消息发送完毕", 50, "*"));
    }
}
