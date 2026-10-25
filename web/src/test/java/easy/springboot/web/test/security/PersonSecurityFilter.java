package easy.springboot.web.test.security;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
import easy.springboot.security.filter.BaseSecurityFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PersonSecurityFilter extends BaseSecurityFilter {

    protected PersonSecurityFilter(BaseSecurityConfigurationSpec configurationSpec) {
        super(configurationSpec);
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        super.doFilterInternal(request, response, filterChain);
    }
}
