package com.youguu.river.common.netty;

import com.youguu.river.common.serialize.MessageCodecUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class MessageObjectEncoder extends MessageToByteEncoder<Object> {

    private MessageCodecUtil util = null;

    public MessageObjectEncoder(final MessageCodecUtil util) {
        this.util = util;
    }

    protected void encode(final ChannelHandlerContext ctx, final Object msg, final ByteBuf out) throws Exception {
        util.encode(out, msg);
    }
}
