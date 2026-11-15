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
import easy.springboot.security.injection.InjectionDetector;
import easy.springboot.security.multipart.FileChecker;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Installs the request wrapper used by the security module.
 *
 * <p>The wrapper performs body-pattern checks only if downstream code reads the
 * request input stream. Multipart file validation occurs when the servlet
 * container or Spring's multipart resolver requests the wrapped request's
 * {@code Part} objects; this filter does not eagerly parse request bodies.</p>
 */
public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    private BaseSecurityConfigurationSpec configurationSpec;
    private FileChecker fileChecker;
    private InjectionDetector injectionDetector;

    @Autowired
    public void setConfigurationSpec(BaseSecurityConfigurationSpec configurationSpec) {
        this.configurationSpec = configurationSpec;
    }

    @Autowired
    public void setFileChecker(FileChecker fileChecker) {
        this.fileChecker = fileChecker;
    }

    @Autowired
    public void setInjectionDetector(InjectionDetector injectionDetector) {
        this.injectionDetector = injectionDetector;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Wrap the request so multipart parts can be checked when parsed downstream.
        filterChain.doFilter(new SecureRequestWrapper(request, configurationSpec, fileChecker, injectionDetector), response);
    }
}
