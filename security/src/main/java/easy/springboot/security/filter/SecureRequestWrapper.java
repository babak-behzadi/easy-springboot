package easy.springboot.security.filter;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import org.jspecify.annotations.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class SecureRequestWrapper extends HttpServletRequestWrapper {

    private final BaseSecurityConfigurationSpec configurationSpec;

    public SecureRequestWrapper(HttpServletRequest request, BaseSecurityConfigurationSpec configurationSpec) {
        super(request);
        this.configurationSpec = configurationSpec;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        byte[] bodyBytes = super.getInputStream().readAllBytes();
        return new BaseCachedInputStream(bodyBytes);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        Collection<Part> parts = super.getParts();
        parts.iterator().forEachRemaining(part -> {

        });
        return parts;
    }

    public static class BaseCachedInputStream extends ServletInputStream {

        private final ByteArrayInputStream input;

        public BaseCachedInputStream(byte[] requestBodyBytes) {
            this.input = new ByteArrayInputStream(requestBodyBytes);
        }

        @Override
        public boolean isFinished() {
            return input.available() == 0;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            // This implementation supports synchronous reads only.
        }

        @Override
        public int read() throws IOException {
            return input.read();
        }

        @Override
        public int read(byte @NonNull [] bytes, int offset, int length) throws IOException {
            return input.read(bytes, offset, length);
        }
    }
}
