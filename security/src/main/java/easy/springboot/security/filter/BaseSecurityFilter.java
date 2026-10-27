package easy.springboot.security.filter;

import easy.springboot.security.config.BaseSecurityConfigurationSpec;
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
 * <p>Multipart validation runs when the servlet container or Spring's multipart
 * resolver asks the wrapped request for its {@code Part} objects. This filter
 * does not eagerly parse every request body itself.</p>
 */
public abstract class BaseSecurityFilter extends OncePerRequestFilter {

    private BaseSecurityConfigurationSpec configurationSpec;
    private FileChecker fileChecker;

    @Autowired
    public void setConfigurationSpec(BaseSecurityConfigurationSpec configurationSpec) {
        this.configurationSpec = configurationSpec;
    }

    @Autowired
    public void setFileChecker(FileChecker fileChecker) {
        this.fileChecker = fileChecker;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        // Wrap the request so multipart parts can be checked when parsed downstream.
        filterChain.doFilter(new SecureRequestWrapper(request, configurationSpec, fileChecker), response);
    }
}
