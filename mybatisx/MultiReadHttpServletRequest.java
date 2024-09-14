package com.spider.interceptor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.io.IOUtils;

public class MultiReadHttpServletRequest extends HttpServletRequestWrapper {
    private ByteArrayOutputStream cachedBytes;

    public MultiReadHttpServletRequest(HttpServletRequest request) {
        super(request);
    }

    public ServletInputStream getInputStream() throws IOException {
        if (this.cachedBytes == null) {
            this.cacheInputStream();
        }

        return new MultiReadHttpServletRequest.CachedServletInputStream(this.cachedBytes.toByteArray());
    }

    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    private void cacheInputStream() throws IOException {
        this.cachedBytes = new ByteArrayOutputStream();
        IOUtils.copy(super.getInputStream(), this.cachedBytes);
    }

    private static class CachedServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream buffer;

        public CachedServletInputStream(byte[] contents) {
            this.buffer = new ByteArrayInputStream(contents);
        }

        public int read() throws IOException {
            return this.buffer.read();
        }

        public boolean isFinished() {
            return this.buffer.available() == 0;
        }

        public boolean isReady() {
            return true;
        }

        public void setReadListener(ReadListener listener) {
            throw new RuntimeException("Not implemented");
        }
    }
}