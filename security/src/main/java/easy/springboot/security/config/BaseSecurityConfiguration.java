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

/**
 * Configures the module's servlet security filter chain, CSRF/CORS behavior,
 * excluded request matchers, and Spring method-security support.
 *
 * <p>Applications still need to supply their own authentication mechanism and
 * review these settings for their deployment.</p>
 */
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
