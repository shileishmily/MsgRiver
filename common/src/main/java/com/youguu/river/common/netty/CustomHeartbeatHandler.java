package com.youguu.river.common.netty;

import com.youguu.river.common.model.MessageSource;
import com.youguu.river.common.model.MessageType;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.concurrent.atomic.AtomicLong;

public abstract class CustomHeartbeatHandler extends ChannelInboundHandlerAdapter {
    public static final byte PING_MSG = 1;
    public static final byte PONG_MSG = 2;
    public static final byte CUSTOM_MSG = 3;
    private int heartbeatCount = 0;
    private AtomicLong msgId = new AtomicLong(0L);
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 发送心跳
     * @param context
     */
    protected void sendPingMsg(ChannelHandlerContext context) {
        RequestMessage request = new RequestMessage();
        request.setMsgId(String.valueOf(msgId.incrementAndGet()));
        request.setMsgSource(MessageSource.AvatarMQConsumer);
        request.setMsgType(MessageType.AvatarMQHeartBeat);

        context.writeAndFlush(request);

        System.out.println("sent ping msg to " + context.channel().remoteAddress() + ", count: " + msgId.get());
    }

    /**
     * 响应心跳
     * @param context
     */
    protected void sendPongMsg(ChannelHandlerContext context) {
        ResponseMessage response = new ResponseMessage();
        response.setMsgId(String.valueOf(msgId.incrementAndGet()));
        response.setMsgSource(MessageSource.AvatarMQBroker);
        response.setMsgType(MessageType.AvatarMQHeartBeat);

        context.writeAndFlush(response);
        System.out.println("sent pong msg to " + context.channel().remoteAddress() + ", count: " + msgId.get());
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        System.err.println("---READER_IDLE---");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        System.err.println("---WRITER_IDLE---");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        System.err.println("---ALL_IDLE---");
    }
}
