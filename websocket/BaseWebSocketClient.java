package com.spider.ws;

import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

@Slf4j
public class BaseWebSocketClient extends WebSocketClient {

    public BaseWebSocketClient(URI uri) {
        super(uri);
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        log.info("连接到" + getURI()+"成功");
    }

    @Override
    public void onMessage(String message) {
        log.info("连接到" + getURI()+"消息:"+message);
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        log.info("连接到" + getURI()+"关闭,code"+code+",reason:"+reason+",remote:"+remote);
    }

    @Override
    public void onError(Exception ex) {
        log.info("连接到" + getURI()+"错误,"+ex.getMessage());
        log.error(ex.getMessage(),ex);
    }
}
