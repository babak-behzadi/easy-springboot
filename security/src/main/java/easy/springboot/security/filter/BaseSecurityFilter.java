package easy.springboot.security.filter;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    private final BaseSecurityConfigurationSpec configurationSpec;

    protected BaseSecurityFilter(BaseSecurityConfigurationSpec configurationSpec) {
        this.configurationSpec = configurationSpec;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(new SecureRequestWrapper(request, configurationSpec), response);
    }

}
