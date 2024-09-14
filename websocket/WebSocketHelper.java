package com.spider.ws;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.java_websocket.client.WebSocketClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class WebSocketHelper {


    /**
     * @Description: 使用okhttp的websocket
     * @param url 例如 wss://push.coinmarketcap.com/ws?device=web&client_source=home_page
     * @param  webSocketListener 例如OkHttpWebSocketListener
     * @author Hao.Yuan
     * @date 2024/9/14
     */
    public  WebSocket useOkHttp(String url, WebSocketListener webSocketListener){
        Request request = new Request.Builder().url(url).build();
        WebSocket webSocket = OkHttpWebSocket.createWebSocket(request,webSocketListener);
        return webSocket;
    }
    /**
     * @Description: 使用基础的websocket
     * @param webSocketClient 例如BaseWebSocketClient 内部uri参数 例如 wss://stream.binance.com:9443/stream?streams=ethbtc@ticker/ethbtc@depth20/trxbtc@ticker/trxbtc@depth20
     * @author Hao.Yuan
     * @date 2024/9/14
     */
    public  WebSocketClient useBase(WebSocketClient webSocketClient) {
        // 使用原生
        String url = webSocketClient.getURI().getPath();
        webSocketClient.connect();
        int i=0;
        while (!webSocketClient.isOpen()) {
            i++;
            log.info(url+",连接中...");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(i>11){
                // 失败
                log.info(url+",链接失败");
                webSocketClient.close();
                break;
            }
        }
        if(webSocketClient.isOpen()){
            log.info(url+",链接成功");
        }
        return webSocketClient;
    }
}
