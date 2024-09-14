package com.spider.ws;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

@Slf4j
public class OkHttpWebSocketListener extends WebSocketListener {
    @Override
    public void onMessage(WebSocket webSocket,String message){
        log.info("接收到消息:"+message);
    }
    @Override
    public void onOpen(WebSocket webSocket, Response response){
        log.info("成功");
        webSocket.send("{\"method\":\"RSUBSCRIPTION\",\"params\":[\"main-site@crypto_price_5s@{}@normal\",\"1\"]}");
    }
    @Override
    public void onClosed(WebSocket webSocket,int code,String reason){
        log.info("关闭:"+reason);
    }
    @Override
    public void onFailure(WebSocket webSocket,Throwable throwable,Response response){
        log.info("链接失败!");
    }
}
