package easy.springboot.security.restriction;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service("authorityRestrictionService")
public class AuthorityRestrictionService {

    /**
     * check if current authentication has grated authorities to target methods
     *
     * @param target target object
     * @return boolean
     */
    public boolean hasAccess(Object target) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        AuthorityRestriction restriction = target.getClass().getAnnotation(AuthorityRestriction.class);
        List<String> authorities = Arrays.asList(restriction.value());
        if (authorities.isEmpty()) {
            return true;
        }

        List<String> grantedAuthorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        return authorities.stream().anyMatch(grantedAuthorities::contains);
    }
}
