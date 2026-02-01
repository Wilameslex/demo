package com.example.demo.filter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 缓存请求体的HttpServletRequest包装类，解决请求流只能读取一次的问题
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {
    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        // 读取原始请求流并缓存（自己实现，无需依赖第三方库）
        this.cachedBody = readInputStream(request.getInputStream());
    }

    // 重写getInputStream，返回缓存的流（供后续组件读取）
    @Override
    public ServletInputStream getInputStream() throws IOException {
        return new CachedBodyServletInputStream(this.cachedBody);
    }

    // 重写getReader，返回缓存的流（供后续组件读取）
    @Override
    public java.io.BufferedReader getReader() throws IOException {
        return new java.io.BufferedReader(
                new java.io.InputStreamReader(new ByteArrayInputStream(this.cachedBody))
        );
    }

    // 工具方法：读取InputStream到字节数组
    private byte[] readInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, len);
        }
        return byteArrayOutputStream.toByteArray();
    }

    // 自定义ServletInputStream，读取缓存的请求体
    private static class CachedBodyServletInputStream extends ServletInputStream {
        private final ByteArrayInputStream byteArrayInputStream;

        public CachedBodyServletInputStream(byte[] cachedBody) {
            this.byteArrayInputStream = new ByteArrayInputStream(cachedBody);
        }

        @Override
        public int read() throws IOException {
            return this.byteArrayInputStream.read();
        }

        @Override
        public boolean isFinished() {
            return this.byteArrayInputStream.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // 无需实现
        }
    }
}