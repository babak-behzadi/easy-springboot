package easy.springboot.security.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;

import java.util.Arrays;

@ConfigurationProperties(prefix = "spring.easy.security")
public class BaseSecurityConfigurationSpec {

    private BaseFilterConfiguration filter;
    private BaseCsrfConfiguration csrf;
    private BaseCorsConfiguration cors;
    private BaseMultipartConfiguration multipart;

    public BaseFilterConfiguration getFilter() {
        return filter;
    }

    public void setFilter(BaseFilterConfiguration baseFilterConfiguration) {
        this.filter = baseFilterConfiguration;
    }

    public BaseCsrfConfiguration getCsrf() {
        return csrf;
    }

    public void setCsrf(BaseCsrfConfiguration csrf) {
        this.csrf = csrf;
    }

    public BaseCorsConfiguration getCors() {
        return cors;
    }

    public void setCors(BaseCorsConfiguration cors) {
        this.cors = cors;
    }

    public BaseMultipartConfiguration getMultipart() {
        return multipart;
    }

    public void setMultipart(BaseMultipartConfiguration multipart) {
        this.multipart = multipart;
    }

    public static class BaseFilterConfiguration {

        private String[] excludedUrls;

        public String[] getExcludedUrls() {
            return excludedUrls;
        }

        public void setExcludedUrls(String[] excludedUrls) {
            this.excludedUrls = excludedUrls;
        }
    }

    public static class BaseCsrfConfiguration {

        private boolean enabled = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class BaseCorsConfiguration {

        private boolean enabled = true;
        private String[] origins = {""};
        private String[] methods = Arrays.stream(HttpMethod.values()).map(HttpMethod::name).toList().toArray(new String[0]);
        private String[] headers = {""};
        private boolean withCredentials = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String[] getOrigins() {
            return origins;
        }

        public void setOrigins(String[] origins) {
            this.origins = origins;
        }

        public String[] getMethods() {
            return methods;
        }

        public void setMethods(String[] methods) {
            this.methods = methods;
        }

        public String[] getHeaders() {
            return headers;
        }

        public void setHeaders(String[] headers) {
            this.headers = headers;
        }

        public boolean isWithCredentials() {
            return withCredentials;
        }

        public void setWithCredentials(boolean withCredentials) {
            this.withCredentials = withCredentials;
        }
    }

    public static class BaseMultipartConfiguration {

        private String[] validFileExtensions;

        public String[] getValidFileExtensions() {
            return validFileExtensions;
        }

        public void setValidFileExtensions(String[] validFileExtensions) {
            this.validFileExtensions = validFileExtensions;
        }
    }
}
