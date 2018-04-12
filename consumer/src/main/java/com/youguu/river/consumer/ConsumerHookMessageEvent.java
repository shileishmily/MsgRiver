package com.youguu.river.consumer;

import com.youguu.river.common.core.HookMessageEvent;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.msg.Message;

public class ConsumerHookMessageEvent extends HookMessageEvent<Object> {

    private ProducerMessageHook hook;

    public ConsumerHookMessageEvent(ProducerMessageHook hook) {
        this.hook = hook;
    }

    public Object callBackMessage(Object obj) {
        ResponseMessage response = (ResponseMessage) obj;
        if (response.getMsgParams() instanceof Message) {
            ConsumerAckMessage result = hook.hookMessage((Message) response.getMsgParams());
            result.setMsgId(((Message) response.getMsgParams()).getMsgId());
            return result;
        } else {
            return null;
        }
    }
}
