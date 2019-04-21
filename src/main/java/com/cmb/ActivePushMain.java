package com.cmb;

import com.cmb.transport.CollectChannelInitializer;
import com.cmb.transport.WsChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

public class ActivePushMain {
    private static final Logger LOGGER = LoggerFactory.getLogger(ActivePushMain.class);

    private static HashSet<Channel> wsChannelSet = new HashSet<>();

    public static void main(String[] args) {
        NioEventLoopGroup collectEventLoop = new NioEventLoopGroup(1);
        NioEventLoopGroup wsEventLoop = new NioEventLoopGroup(1);

        try {
            ServerBootstrap collectServerBootstrap = new ServerBootstrap();
            collectServerBootstrap.group(collectEventLoop)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new CollectChannelInitializer(wsChannelSet));

            ChannelFuture future = collectServerBootstrap
                    .bind(8080);
            LOGGER.info("Data Collect Server Startup at port:8080");

            ServerBootstrap wsServerBootstrap = new ServerBootstrap();
            wsServerBootstrap.group(wsEventLoop)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new WsChannelInitializer(wsChannelSet));

            ChannelFuture wsFuture = wsServerBootstrap
                    .bind(8081);
            LOGGER.info("Web Socket Server Startup at port:8081");

            wsFuture.channel().closeFuture().sync();
            future.channel().closeFuture().sync();



        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException:", e);
        } finally {
            collectEventLoop.shutdownGracefully();
            wsEventLoop.shutdownGracefully();
        }
    }
}
