package com.youguu.river.common.launcher;

import com.youguu.river.common.core.CallBackInvoker;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

public class LauncherListener implements ChannelFutureListener {

    private CallBackInvoker<Object> invoke = null;

    public LauncherListener(CallBackInvoker<Object> invoke) {
        this.invoke = invoke;
    }

    public void operationComplete(ChannelFuture future) throws Exception {
        if (!future.isSuccess()) {
            invoke.setReason(future.cause());
        }
    }
}
