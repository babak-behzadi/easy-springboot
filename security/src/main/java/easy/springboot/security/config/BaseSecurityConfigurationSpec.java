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
                                            BaseMultipartConfiguration multipart) {

    /** Paths excluded from the configured authentication requirement. */
    public record BaseFilterConfiguration(String[] excludedUrls) {

        public BaseFilterConfiguration(String[] excludedUrls) {
            this.excludedUrls = excludedUrls == null ? new String[0] : excludedUrls;
        }
    }

    /** Controls whether CSRF protection is enabled. */
    public record BaseCsrfConfiguration(boolean enabled) {
    }

    /** Controls CORS behavior for the consuming application. */
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
            this.origins = origins == null ? new String[]{"**"} : origins;
            this.methods = methods == null ? new String[]{"*"} : methods;
            this.headers = headers == null ? new String[]{"*"} : headers;
            this.withCredentials = withCredentials;
        }
    }

    /** Controls multipart MIME-type checks and the extensions used to build the allowlist. */
    public record BaseMultipartConfiguration(boolean enabled,
                                             String[] fileExtensions) {

        public BaseMultipartConfiguration(boolean enabled, String[] fileExtensions) {
            this.enabled = enabled;
            this.fileExtensions = fileExtensions == null ? new String[0] : fileExtensions;
        }
    }
}
