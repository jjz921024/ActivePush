package com.cmb.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class HttpHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHandler.class);

    private static HashSet<Channel> webSocketChannel;

    public HttpHandler(HashSet<Channel> webSocketChannel) {
        HttpHandler.webSocketChannel = webSocketChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        if ("/collect".equals(req.uri()) && POST.equals(req.method())) {
            ByteBuf buf = req.content();
            String content = buf.toString(CharsetUtil.UTF_8);
            LOGGER.info("收到http报文: " + content);
            sendResponse(ctx);
            pushMsg(content);
        }
    }

    private void pushMsg(String msg) {
        for (Channel next : webSocketChannel) {
            next.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    private void sendResponse(ChannelHandlerContext ctx) {
        FullHttpResponse resp = new DefaultFullHttpResponse(HTTP_1_1, OK,
                Unpooled.copiedBuffer("ok", CharsetUtil.UTF_8));
        resp.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");

        // 发送完之后关闭连接
        ctx.writeAndFlush(resp).addListener(ChannelFutureListener.CLOSE);
    }
}
