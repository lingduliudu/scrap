package com.spider.ws;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import okhttp3.*;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class OkHttpWebSocket {
    private static Boolean LATENCY_DEBUG_SWATCH = Boolean.FALSE;
    private static ConnectionPool connectionPool = new ConnectionPool(20, 300, TimeUnit.SECONDS);
    private static LinkedBlockingQueue<NetworkLatency> LATENCY_DEBUG_QUEUE = new LinkedBlockingQueue<>();
    private static final OkHttpClient client = new OkHttpClient.Builder().followSslRedirects(false).followRedirects(false).connectTimeout(5000, TimeUnit.MILLISECONDS).readTimeout(5000, TimeUnit.MILLISECONDS).writeTimeout(5000, TimeUnit.MILLISECONDS).connectionPool(connectionPool).addNetworkInterceptor(new Interceptor() {
        @NotNull
        @Override
        public Response intercept(@NotNull Chain chain) throws IOException {
            Request request = chain.request();
            Long startNano = System.nanoTime();
            Response response = chain.proceed(request);
            Long endNano = System.nanoTime();
            if (LATENCY_DEBUG_SWATCH) {
                LATENCY_DEBUG_QUEUE.add(new NetworkLatency(request.url().url().getPath(), startNano, endNano));
            }

            return response;
        }
    }).build();

    public static WebSocket createWebSocket(Request request, WebSocketListener listener) {
        return client.newWebSocket(request, listener);
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class NetworkLatency {

        private String path;

        private Long startNanoTime;

        private Long endNanoTime;
    }
}
