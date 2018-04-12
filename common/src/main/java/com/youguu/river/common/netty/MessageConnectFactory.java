package com.youguu.river.common.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.youguu.river.common.core.CallBackInvoker;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.serialize.KryoCodecUtil;
import com.youguu.river.common.serialize.KryoPoolFactory;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class MessageConnectFactory {

    private SocketAddress remoteAddr = null;
    private ChannelInboundHandlerAdapter messageHandler = null;
    private Map<String, CallBackInvoker<Object>> callBackMap = new ConcurrentHashMap<>();
    private Bootstrap bootstrap = null;
    private long timeout = 10 * 1000;
    private boolean connected = false;
    private EventLoopGroup eventLoopGroup = null;
    private static KryoCodecUtil util = new KryoCodecUtil(KryoPoolFactory.getKryoPoolInstance());
    private Channel messageChannel = null;
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private NettyClustersConfig nettyClustersConfig = new NettyClustersConfig();

    public static final int READ_IDLE_TIME = 0;
    public static final int WRITE_IDLE_TIME = 0;
    public static final int ALL_IDLE_TIME = 5;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder()
            .setNameFormat("MessageConnectFactory-%d")
            .setDaemon(true)
            .build();

    public MessageConnectFactory(String serverAddress) {
        String[] ipAddr = serverAddress.split(MessageSystemConfig.IpV4AddressDelimiter);
        if (ipAddr.length == 2) {
            remoteAddr = NettyUtil.string2SocketAddress(serverAddress);
        }
    }

    public void setMessageHandle(ChannelInboundHandlerAdapter messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void init() {
        try {
            defaultEventExecutorGroup = new DefaultEventExecutorGroup(NettyClustersConfig.getWorkerThreads(), threadFactory);
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel channel) throws Exception {
                            channel.pipeline().addLast(defaultEventExecutorGroup);
                            channel.pipeline().addLast(new IdleStateHandler(READ_IDLE_TIME, WRITE_IDLE_TIME, ALL_IDLE_TIME));
                            channel.pipeline().addLast(new MessageObjectEncoder(util));
                            channel.pipeline().addLast(new MessageObjectDecoder(util));
                            channel.pipeline().addLast(messageHandler);
                        }
                    })
                    .option(ChannelOption.SO_SNDBUF, nettyClustersConfig.getClientSocketSndBufSize())
                    .option(ChannelOption.SO_RCVBUF, nettyClustersConfig.getClientSocketRcvBufSize())
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

//    public void connect() {
//        Preconditions.checkNotNull(messageHandler, "Message's Handler is Null!");
//
//        try {
//            init();
//            ChannelFuture channelFuture = bootstrap.connect(this.remoteAddr).sync();
//
//            channelFuture.addListener(new ChannelFutureListener() {
//                public void operationComplete(ChannelFuture future) throws Exception {
//                    Channel channel = future.channel();
//                    messageChannel = channel;
//                }
//            });
//
//            System.out.println("ip address:" + this.remoteAddr.toString());
//            connected = true;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 重连机制,每隔2s重新连接一次服务器
     */
    public void connect() {
        try {
            if (messageChannel != null && messageChannel.isActive()) {
                return;
            }

            init();
            ChannelFuture channelFuture = bootstrap.connect(this.remoteAddr).sync();

            channelFuture.addListener((ChannelFutureListener) futureListener -> {
                if (futureListener.isSuccess()) {
                    messageChannel = futureListener.channel();
                    System.out.println("Connect to server successfully!");
                } else {
                    System.out.println("Failed to connect to server, try connect after 2s");
                    futureListener.channel().eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            connect();
                        }
                    }, 2, TimeUnit.SECONDS);
                }
            });

            System.out.println("ip address:" + this.remoteAddr.toString());
            connected = true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void close() {
        if (messageChannel != null) {
            try {
                messageChannel.close().sync();
                eventLoopGroup.shutdownGracefully();
                defaultEventExecutorGroup.shutdownGracefully();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean traceInvoker(String key) {
        if (key == null) {
            return false;
        }
        return getCallBackMap().containsKey(key);
    }

    public CallBackInvoker<Object> detachInvoker(String key) {
        if (traceInvoker(key)) {
            return getCallBackMap().remove(key);
        } else {
            return null;
        }
    }

    public void setTimeOut(long timeout) {
        this.timeout = timeout;
    }

    public long getTimeOut() {
        return this.timeout;
    }

    public Channel getMessageChannel() {
        return messageChannel;
    }

    public Map<String, CallBackInvoker<Object>> getCallBackMap() {
        return callBackMap;
    }

    public void setCallBackMap(Map<String, CallBackInvoker<Object>> callBackMap) {
        this.callBackMap = callBackMap;
    }
}
