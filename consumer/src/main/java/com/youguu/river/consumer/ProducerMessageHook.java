package com.youguu.river.consumer;

import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.msg.Message;

public interface ProducerMessageHook {

    ConsumerAckMessage hookMessage(Message paramMessage);
}
