package com.cmb.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.HashSet;

/**
 * 配置channelHandler链
 */
public class WsChannelInitializer extends ChannelInitializer<Channel> {

    private static HashSet<Channel> wsChannelSet;

    public WsChannelInitializer(HashSet<Channel> wsChannelSet) {
        WsChannelInitializer.wsChannelSet = wsChannelSet;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec())  // 编解码http
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ChunkedWriteHandler())
                //.addLast(new WebSocketServerProtocolHandler("/websocket"));
                .addLast(new WebSocketServerHandler(wsChannelSet));
    }
}
