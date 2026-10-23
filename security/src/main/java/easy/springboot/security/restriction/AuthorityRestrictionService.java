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

package easy.springboot.security.restriction;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Evaluates the current authentication against a target class's authority restriction.
 */
@Service("authorityRestrictionService")
public class AuthorityRestrictionService {

    private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();

    /**
     * Returns whether the current non-anonymous authentication satisfies the target's
     * {@link AuthorityRestriction}. Missing targets or restrictions are denied.
     *
     * @param target target controller or proxied controller
     * @return {@code true} when access is allowed; otherwise {@code false}
     */
    public boolean hasAccess(Object target) {
        if (target == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || trustResolver.isAnonymous(authentication)) {
            return false;
        }

        AuthorityRestriction restriction = AnnotatedElementUtils.findMergedAnnotation(
                AopUtils.getTargetClass(target), AuthorityRestriction.class);
        if (restriction == null) {
            return false;
        }

        List<String> requiredAuthorities = Arrays.asList(restriction.value());
        if (requiredAuthorities.isEmpty()) {
            return true;
        }

        List<String> grantedAuthorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return requiredAuthorities.stream().anyMatch(grantedAuthorities::contains);
    }
}
