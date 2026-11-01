/*
 * Copyright 2026 Babak Behzadi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package easy.springboot.security.filter;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import easy.springboot.security.exception.InvalidFileType;
import easy.springboot.security.exception.MaliciousContentException;
import easy.springboot.security.injection.InjectionDetector;
import easy.springboot.security.multipart.FileChecker;
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

/**
 * Decorates an HTTP request with optional request-body pattern checks and multipart
 * file MIME-type validation.
 *
 * <p>The injection-pattern check runs only when downstream code reads
 * {@link #getInputStream()}; it does not inspect every parameter source. Multipart
 * validation depends on the servlet container parsing parts through {@link #getParts()}.
 * Container parsing errors can occur before a file MIME-type check is performed.</p>
 */
public final class SecureRequestWrapper extends HttpServletRequestWrapper {

    private final BaseSecurityConfigurationSpec configurationSpec;
    private final FileChecker fileChecker;
    private final InjectionDetector injectionDetector;

    public SecureRequestWrapper(HttpServletRequest request,
                                BaseSecurityConfigurationSpec configurationSpec,
                                FileChecker fileChecker,
                                InjectionDetector injectionDetector) {
        super(request);
        this.configurationSpec = configurationSpec;
        this.fileChecker = fileChecker;
        this.injectionDetector = injectionDetector;
    }

    /**
     * Reads and buffers the request body, optionally rejecting it when a built-in
     * injection heuristic matches. The body is held in memory for replay by callers.
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        // Buffer the request body so this wrapper can provide a stream over the captured bytes.
        byte[] bodyBytes = super.getInputStream().readAllBytes();
        if (configurationSpec.injection().enabled() &&
                injectionDetector.checkInjection(bodyBytes)) {
            throw new MaliciousContentException();
        }
        return new BaseCachedInputStream(bodyBytes);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return super.getParameterMap();
    }

    /**
     * Parses multipart data using the underlying servlet request and validates
     * uploaded file parts when multipart checking is enabled.
     */
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        // Let the servlet container parse multipart data, including its boundary and parts.
        Collection<Part> parts = super.getParts();
        if (configurationSpec.multipart().enabled()) {
            for (Part part : parts) {
                // Regular form fields (for example, "id=1") are not uploaded files.
                if (part.getSubmittedFileName() == null) {
                    continue;
                }
                // Reject the request as soon as an uploaded file fails the MIME-type allowlist.
                if (!fileChecker.check(part)) {
                    throw new InvalidFileType();
                }
            }
        }
        return parts;
    }

    /**
     * A synchronous ServletInputStream backed by an in-memory copy of request bytes.
     *
     * <p>This implementation does not support non-blocking reads; {@code setReadListener}
     * is intentionally a no-op.</p>
     */
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
