package smart_on_fhir_server.smart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class SmartScopeServiceTest {

    private final SmartScopeService service =
            new SmartScopeService();

    @Test
    void parsesUnconstrainedObservationScope() {

        SmartScope scope =
                service.parseScope(
                        "patient/Observation.rs"
                );

        assertEquals("patient", scope.type());
        assertEquals("Observation", scope.resource());
        assertEquals("rs", scope.permissions());
        assertTrue(scope.constraints().isEmpty());
    }

    @Test
    void parsesCodeConstraint() {

        SmartScope scope =
                service.parseScope(
                        "patient/Observation.rs?code=heart-rate"
                );

        assertEquals("patient", scope.type());
        assertEquals("Observation", scope.resource());
        assertEquals("rs", scope.permissions());
        assertEquals(
                "heart-rate",
                scope.constraint("code")
        );
    }

    @Test
    void findsObservationSearchScopeFromTokenScopes() {

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("smart-client")
                        .claim(
                                "scope",
                                java.util.List.of(
                                        "openid",
                                        "launch/patient",
                                        "patient/*.read",
                                        "patient/Observation.rs"
                                )
                        )
                        .claim("patient", "patient-1")
                        .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        new org.springframework.security.oauth2.jwt.Jwt(
                                "token",
                                java.time.Instant.now(),
                                java.time.Instant.now().plusSeconds(300),
                                java.util.Map.of("alg", "none"),
                                claims.getClaims()
                        )
                );

        SmartScope scope =
                service.findPatientSearchScope(
                        authentication,
                        "Observation"
                );

        assertEquals("patient", scope.type());
        assertEquals("Observation", scope.resource());
        assertEquals("rs", scope.permissions());
    }

    @Test
    void ignoresNonResourceScopes() {

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("smart-client")
                        .claim(
                                "scope",
                                java.util.List.of(
                                        "openid",
                                        "launch/patient"
                                )
                        )
                        .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        new org.springframework.security.oauth2.jwt.Jwt(
                                "token",
                                java.time.Instant.now(),
                                java.time.Instant.now().plusSeconds(300),
                                java.util.Map.of("alg", "none"),
                                claims.getClaims()
                        )
                );

        SmartScope scope =
                service.findPatientSearchScope(
                        authentication,
                        "Observation"
                );

        org.junit.jupiter.api.Assertions.assertNull(scope);
    }

    @Test
    void readScopeDoesNotGrantSearchAccess() {

        JwtClaimsSet claims =
                JwtClaimsSet.builder()
                        .subject("smart-client")
                        .claim(
                                "scope",
                                java.util.List.of(
                                        "patient/Observation.read"
                                )
                        )
                        .build();

        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        new org.springframework.security.oauth2.jwt.Jwt(
                                "token",
                                java.time.Instant.now(),
                                java.time.Instant.now().plusSeconds(300),
                                java.util.Map.of("alg", "none"),
                                claims.getClaims()
                        )
                );

        SmartScope scope =
                service.findPatientSearchScope(
                        authentication,
                        "Observation"
                );

        org.junit.jupiter.api.Assertions.assertNull(scope);
    }
}