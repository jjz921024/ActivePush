package com.cmb.transport;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.ContinuationWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import java.util.HashSet;

/**
 * 配置channelHandler链
 */
public class CollectChannelInitializer extends ChannelInitializer<Channel> {

    private static HashSet<Channel> wsChannelSet;

    public CollectChannelInitializer(HashSet<Channel> wsChannelSet) {
        CollectChannelInitializer.wsChannelSet = wsChannelSet;
    }

    @Override
    protected void initChannel(Channel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec())  // 编解码http
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new HttpHandler(wsChannelSet));  //处理http请求
    }
}
