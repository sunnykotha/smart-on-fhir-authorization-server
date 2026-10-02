package smart_on_fhir_server.observation.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import smart_on_fhir_server.observation.Observation;
import smart_on_fhir_server.observation.repository.ObservationRepository;
import smart_on_fhir_server.smart.SmartScopeService;

class ObservationServiceTest {

    @Test
    void constrainedScopeForcesAuthorizedCode() {

        ObservationRepository repository =
                Mockito.mock(ObservationRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        ObservationService service =
                new ObservationService(
                        repository,
                        scopes
                );

        Observation observation =
                new Observation();

        when(repository.findByPatientIdAndCode(
                "patient-1",
                "heart-rate"
        )).thenReturn(List.of(observation));

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/patient",
                                "patient/Observation.rs?code=heart-rate"
                        ),
                        "patient-1"
                );

        service.search(authentication, null);

        verify(repository).findByPatientIdAndCode(
                "patient-1",
                "heart-rate"
        );
    }

    @Test
    void callerCannotOverrideAuthorizedCodeConstraint() {

        ObservationRepository repository =
                Mockito.mock(ObservationRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        ObservationService service =
                new ObservationService(
                        repository,
                        scopes
                );

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "patient/Observation.rs?code=heart-rate"
                        ),
                        "patient-1"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> service.search(
                        authentication,
                        "blood-pressure"
                )
        );

        Mockito.verifyNoInteractions(repository);
    }

    private JwtAuthenticationToken authentication(
            List<String> scopes,
            String patient) {

        Jwt jwt =
                new Jwt(
                        "test-token",
                        Instant.now(),
                        Instant.now().plusSeconds(300),
                        Map.of("alg", "none"),
                        Map.of(
                                "scope", scopes,
                                "patient", patient,
                                "sub", "smart-client"
                        )
                );

        return new JwtAuthenticationToken(jwt);
    }
}