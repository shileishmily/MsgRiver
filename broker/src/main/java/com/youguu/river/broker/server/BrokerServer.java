package com.youguu.river.broker.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.youguu.river.broker.ConsumerMessageHook;
import com.youguu.river.broker.MessageBrokerHandler;
import com.youguu.river.broker.ProducerMessageHook;
import com.youguu.river.common.core.MessageSystemConfig;
import com.youguu.river.common.netty.MessageObjectDecoder;
import com.youguu.river.common.netty.MessageObjectEncoder;
import com.youguu.river.common.netty.NettyClustersConfig;
import com.youguu.river.common.netty.NettyUtil;
import com.youguu.river.common.serialize.KryoCodecUtil;
import com.youguu.river.common.serialize.KryoPoolFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务端启动类(入口)
 */
public class BrokerServer extends BrokerParallelServer implements RemotingServer{
    private ThreadFactory threadBossFactory = new ThreadFactoryBuilder().setNameFormat("AvatarMQBroker[BossSelector]-%d").setDaemon(true).build();

    private ThreadFactory threadWorkerFactory = new ThreadFactoryBuilder().setNameFormat("AvatarMQBroker[WorkerSelector]-%d").setDaemon(true) .build();

    private int brokerServerPort = 0;
    private ServerBootstrap bootstrap;
    private MessageBrokerHandler handler;
    private SocketAddress serverIpAddr;
    private NettyClustersConfig nettyClustersConfig = new NettyClustersConfig();
    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private EventLoopGroup boss;
    private EventLoopGroup workers;

    public BrokerServer(String serverAddress) {
        String[] ipAddr = serverAddress.split(MessageSystemConfig.IpV4AddressDelimiter);

        if (ipAddr.length == 2) {
            serverIpAddr = NettyUtil.string2SocketAddress(serverAddress);
        }
    }

    @Override
    public void init() {
        try {
            handler = new MessageBrokerHandler().buildConsumerHook(new ConsumerMessageHook()).buildProducerHook(new ProducerMessageHook());

            boss = new NioEventLoopGroup(1, threadBossFactory);

            workers = new NioEventLoopGroup(parallel, threadWorkerFactory, NettyUtil.getNioSelectorProvider());

            KryoCodecUtil util = new KryoCodecUtil(KryoPoolFactory.getKryoPoolInstance());

            bootstrap = new ServerBootstrap();

            bootstrap.group(boss, workers).channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .option(ChannelOption.SO_KEEPALIVE, false)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_SNDBUF, nettyClustersConfig.getClientSocketSndBufSize())
                    .option(ChannelOption.SO_RCVBUF, nettyClustersConfig.getClientSocketRcvBufSize())
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .localAddress(serverIpAddr)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    defaultEventExecutorGroup,
                                    new IdleStateHandler(10, 0, 0),
                                    new MessageObjectEncoder(util),
                                    new MessageObjectDecoder(util),
                                    handler);
                        }
                    });

            super.init();
        } catch (IOException ex) {
            Logger.getLogger(BrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int localListenPort() {
        return brokerServerPort;
    }

    @Override
    public void shutdown() {
        try {
            super.shutdown();
            boss.shutdownGracefully();
            workers.shutdownGracefully();
            defaultEventExecutorGroup.shutdownGracefully();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("AvatarMQBrokerServer shutdown exception!");
        }
    }

    @Override
    public void start() {
        try {
            String ipAddress = NettyUtil.socketAddress2String(serverIpAddr);
            System.out.printf("broker server ip:[%s]\n", ipAddress);

            ChannelFuture sync = this.bootstrap.bind().sync();

            super.start();

            sync.channel().closeFuture().sync();
            InetSocketAddress addr = (InetSocketAddress) sync.channel().localAddress();
            brokerServerPort = addr.getPort();
        } catch (InterruptedException ex) {
            Logger.getLogger(BrokerServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
