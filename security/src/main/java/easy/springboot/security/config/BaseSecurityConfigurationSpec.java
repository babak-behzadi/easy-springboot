package easy.springboot.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.easy.security")
public record BaseSecurityConfigurationSpec(BaseFilterConfiguration filter,
                                            BaseCsrfConfiguration csrf,
                                            BaseCorsConfiguration cors,
                                            BaseMultipartConfiguration multipart) {

    public record BaseFilterConfiguration(String[] excludedUrls) {

        public BaseFilterConfiguration(String[] excludedUrls) {
            this.excludedUrls = excludedUrls == null ? new String[0] : excludedUrls;
        }
    }

    public record BaseCsrfConfiguration(boolean enabled) {
    }

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

    public record BaseMultipartConfiguration(boolean enabled,
                                             String[] fileExtensions) {

        public BaseMultipartConfiguration(boolean enabled, String[] fileExtensions) {
            this.enabled = enabled;
            this.fileExtensions = fileExtensions == null ? new String[0] : fileExtensions;
        }
    }
}
