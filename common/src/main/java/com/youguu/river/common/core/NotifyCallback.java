package com.youguu.river.common.core;

import com.youguu.river.common.msg.ProducerAckMessage;

public interface NotifyCallback {

    void onEvent(ProducerAckMessage result);
}
