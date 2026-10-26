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

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Evaluates the current authentication against a target class's authority restriction.
 */
@Component
@Aspect
public class BaseEndpointsAuthorityRestrictionAdvice {

    @Pointcut("@annotation(PreAuthorizeBaseEndpoint)")
    public void pointcut() {
    }

    @Around("pointcut()")
    public Object check(ProceedingJoinPoint joinPoint) throws Throwable {
        if (hasAccess(joinPoint.getTarget(), joinPoint.getSignature().getName())) {
            return joinPoint.proceed();
        }
        throw new AccessDeniedException("Access denied");
    }

    /**
     * Returns whether the current non-anonymous authentication satisfies the target's
     * {@link PreAuthorizeBaseEndpoint}. Missing targets or restrictions are denied.
     *
     * @param target target controller or proxied controller
     * @return {@code true} when access is allowed; otherwise {@code false}
     */
    public boolean hasAccess(Object target, String endpoint) {
        if (target == null) {
            return false;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        BaseEndpointsAuthorityRestriction restriction = target.getClass()
                .getDeclaredAnnotation(BaseEndpointsAuthorityRestriction.class);
        if (restriction == null) {
            return false;
        }

        Optional<BaseEndpoint> baseEndpoint = Arrays.stream(restriction.value())
                .filter(e -> e.value().equals(endpoint)).findFirst();

        if (baseEndpoint.isEmpty()) {
            return false;
        }

        List<String> grantedAuthorities = authentication.getAuthorities()
                .stream().map(GrantedAuthority::getAuthority).toList();

        return new HashSet<>(grantedAuthorities).containsAll(Arrays.asList(baseEndpoint.get().authorities()));
    }
}
