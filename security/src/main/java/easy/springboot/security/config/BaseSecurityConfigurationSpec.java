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

package easy.springboot.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Binds the reusable security module's settings from {@code spring.easy.security}.
 *
 * <p>Nested records group filter exclusions, CSRF, CORS, and multipart settings.</p>
 */
@ConfigurationProperties(prefix = "spring.easy.security")
public record BaseSecurityConfigurationSpec(BaseFilterConfiguration filter,
                                            BaseCsrfConfiguration csrf,
                                            BaseCorsConfiguration cors,
                                            BaseMultipartConfiguration multipart,
                                            BaseInjectionConfiguration injection) {

    /**
     * Paths excluded from the configured authentication requirement.
     */
    public record BaseFilterConfiguration(String[] excludedUrls) {

        public BaseFilterConfiguration(String[] excludedUrls) {
            this.excludedUrls = excludedUrls == null ? new String[0] : excludedUrls;
        }
    }

    /**
     * Controls whether CSRF protection is enabled.
     */
    public record BaseCsrfConfiguration(boolean enabled) {
    }

    /**
     * Controls CORS behavior for the consuming application.
     */
    public record BaseCorsConfiguration(boolean enabled,
                                        String[] origins,
                                        String[] methods,
                                        String[] headers,
                                        boolean withCredentials) {

        public BaseCorsConfiguration(boolean enabled,
                                     String[] origins,
                                     String[] methods,
                                     String[] headers,
                                     boolean withCredentials) {
            this.enabled = enabled;
            this.origins = origins == null ? new String[]{"/**"} : origins;
            this.methods = methods == null ? new String[]{"*"} : methods;
            this.headers = headers == null ? new String[]{"*"} : headers;
            this.withCredentials = withCredentials;
        }
    }

    /**
     * Controls multipart MIME-type checks and the extensions used to build the allowlist.
     */
    public record BaseMultipartConfiguration(boolean enabled,
                                             String[] fileExtensions) {

        public BaseMultipartConfiguration(boolean enabled, String[] fileExtensions) {
            this.enabled = enabled;
            this.fileExtensions = fileExtensions == null ? new String[0] : fileExtensions;
        }
    }

    /**
     * Controls the built-in request-body pattern checks.
     *
     * <p>{@code customPattern} is currently bound from configuration but is not
     * consumed by {@code InjectionDetector}.</p>
     */
    public record BaseInjectionConfiguration(boolean enabled, String customPattern) {
    }
}
