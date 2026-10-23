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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthorityRestrictionServiceTest {

    private final AuthorityRestrictionService service = new AuthorityRestrictionService();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deniesWhenNoAuthenticationExists() {
        assertFalse(service.hasAccess(new UserController()));
    }

    @Test
    void deniesWhenTargetIsNull() {
        authenticateUser("ROLE_USER");
        assertFalse(service.hasAccess(null));
    }

    @Test
    void deniesWhenTargetHasNoRestriction() {
        authenticateUser("ROLE_USER");
        assertFalse(service.hasAccess(new UnrestrictedController()));
    }

    @Test
    void allowsWhenAtLeastOneRequiredAuthorityMatches() {
        authenticateUser("ROLE_USER");
        assertTrue(service.hasAccess(new UserOrAdminController()));
    }

    @Test
    void deniesWhenNoRequiredAuthorityMatches() {
        authenticateUser("ROLE_GUEST");
        assertFalse(service.hasAccess(new UserOrAdminController()));
    }

    @Test
    void emptyRestrictionAllowsAuthenticatedUser() {
        authenticateUser("ROLE_USER");
        assertTrue(service.hasAccess(new AnyAuthenticatedUserController()));
    }

    @Test
    void deniesAnonymousAuthenticationEvenWhenRestrictionIsEmpty() {
        SecurityContextHolder.getContext().setAuthentication(
                new AnonymousAuthenticationToken(
                        "test-key",
                        "anonymousUser",
                        AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertFalse(service.hasAccess(new AnyAuthenticatedUserController()));
    }

    private void authenticateUser(String... authorities) {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("test-user", "password", authorities));
    }

    @AuthorityRestriction("ROLE_USER")
    static class UserController {
    }

    @AuthorityRestriction({"ROLE_USER", "ROLE_ADMIN"})
    static class UserOrAdminController {
    }

    @AuthorityRestriction
    static class AnyAuthenticatedUserController {
    }

    static class UnrestrictedController {
    }
}
