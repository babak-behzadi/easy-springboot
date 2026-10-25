package easy.springboot.security.config;

import easy.springboot.security.filter.BaseSecurityFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties({BaseSecurityConfigurationSpec.class})
public class BaseSecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   BaseSecurityConfigurationSpec spec,
                                                   BaseSecurityFilter filter) throws Exception {
        http.csrf(spec.getCsrf().isEnabled() ? Customizer.withDefaults() : AbstractHttpConfigurer::disable);
        http.cors(spec.getCors().isEnabled() ? Customizer.withDefaults() : AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(
                requests -> requests
                        .requestMatchers(spec.getFilter().getExcludedUrls()).permitAll()
                        .anyRequest().authenticated()
        );
        http.addFilterBefore(filter, SecurityContextHolderFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfiguration corsConfiguration(BaseSecurityConfigurationSpec spec) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        Arrays.stream(spec.getCors().getOrigins())
                .forEach(corsConfiguration::addAllowedOriginPattern);
        corsConfiguration.setAllowedHeaders(List.of(spec.getCors().getHeaders()));
        corsConfiguration.setAllowedMethods(List.of(spec.getCors().getMethods()));
        corsConfiguration.setAllowCredentials(spec.getCors().isWithCredentials());
        return corsConfiguration;
    }

    @Bean
    public FilterRegistrationBean<BaseSecurityFilter> filterRegistration(BaseSecurityFilter filter) {
        FilterRegistrationBean<BaseSecurityFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(filter);
        filterRegistration.addUrlPatterns("/*");
        filterRegistration.setOrder(SecurityFilterProperties.DEFAULT_FILTER_ORDER - 1);
        return filterRegistration;
    }
}
