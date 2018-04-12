package com.youguu.river.common.launcher;

import com.youguu.river.common.core.CallBackInvoker;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.model.ResponseMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

public class SendMessageLauncher {

    private long timeout = MessageSystemConfig.MessageTimeOutValue;

    public Map<String, CallBackInvoker<Object>> invokeMap = new ConcurrentSkipListMap<String, CallBackInvoker<Object>>();

    private SendMessageLauncher() {

    }

    private static SendMessageLauncher resource;

    public static SendMessageLauncher getInstance() {
        if (resource == null) {
            synchronized (SendMessageLauncher.class) {
                if (resource == null) {
                    resource = new SendMessageLauncher();
                }
            }
        }
        return resource;
    }

    public Object launcher(Channel channel, ResponseMessage response) {
        if (channel != null) {
            CallBackInvoker<Object> invoke = new CallBackInvoker<Object>();
            invokeMap.put(response.getMsgId(), invoke);
            invoke.setRequestId(response.getMsgId());
            ChannelFuture channelFuture = channel.writeAndFlush(response);
            channelFuture.addListener(new LauncherListener(invoke));
            try {
                Object result = invoke.getMessageResult(timeout, TimeUnit.MILLISECONDS);
                return result;
            } catch (RuntimeException e) {
                throw e;
            } finally {
                invokeMap.remove(response.getMsgId());
            }
        } else {
            return null;
        }
    }

    public boolean trace(String key) {
        return invokeMap.containsKey(key);
    }

    public CallBackInvoker<Object> detach(String key) {
        if (invokeMap.containsKey(key)) {
            return invokeMap.remove(key);
        } else {
            return null;
        }
    }
}
