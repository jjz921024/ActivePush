package com.cmb.transport;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

/**
 * Author       : 蒋俊钊 80256973
 * Date         : 2019/4/20
 * Copyright    (C) ChinaMerchantsBank
 */
public class WebSocketServerHandler extends SimpleChannelInboundHandler<HttpObject> {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServerHandler.class);

    private WebSocketServerHandshaker handshaker;

    private static HashSet<Channel> webSocketChannel;

    public WebSocketServerHandler(HashSet<Channel> webSocketChannel) {
        WebSocketServerHandler.webSocketChannel = webSocketChannel;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("web socket 连接" + ctx.channel());
        webSocketChannel.add(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("web socket 断开" + ctx.channel());
        webSocketChannel.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        LOGGER.info("websocket handler 收到报文");
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    // 处理websocket请求
    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }

        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException("not support");
        }

        String text = ((TextWebSocketFrame) frame).text();
        ctx.channel().writeAndFlush(new TextWebSocketFrame("-->" + text));
    }


    // 只处理升级协议的http请求
    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        LOGGER.info("handle http for upgrade");
        // 解码失败或不是升级协议请求
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            LOGGER.info("no upgrade");
            return;
        }

        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                "ws://localhost:8081/websocket",
                null, true, 5 * 1024 * 1024);

        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }



    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
