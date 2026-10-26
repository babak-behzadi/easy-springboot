package easy.springboot.security.config;

import easy.springboot.security.filter.BaseSecurityFilter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
    public SecurityFilterChain securityFilterChain(HttpSecurity http, BaseSecurityConfigurationSpec spec, BaseSecurityFilter filter) {
        http.csrf(spec.csrf().enabled() ? Customizer.withDefaults() : AbstractHttpConfigurer::disable);
        http.cors(spec.cors().enabled() ? Customizer.withDefaults() : AbstractHttpConfigurer::disable);
        http.authorizeHttpRequests(requests -> requests.requestMatchers(spec.filter().excludedUrls()).permitAll().anyRequest().authenticated());
        http.addFilterBefore(filter, SecurityContextHolderFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfiguration corsConfiguration(BaseSecurityConfigurationSpec spec) {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        BaseSecurityConfigurationSpec.BaseCorsConfiguration cors = spec.cors();
        Arrays.stream(cors.origins()).forEach(corsConfiguration::addAllowedOriginPattern);
        corsConfiguration.setAllowedHeaders(List.of(cors.headers()));
        corsConfiguration.setAllowedMethods(List.of(cors.methods()));
        corsConfiguration.setAllowCredentials(cors.withCredentials());
        return corsConfiguration;
    }
}
