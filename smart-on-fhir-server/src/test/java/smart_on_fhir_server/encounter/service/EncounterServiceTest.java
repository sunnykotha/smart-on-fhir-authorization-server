package smart_on_fhir_server.encounter.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import smart_on_fhir_server.encounter.Encounter;
import smart_on_fhir_server.encounter.repository.EncounterRepository;
import smart_on_fhir_server.smart.SmartScopeService;

class EncounterServiceTest {

    @Test
    void authorizedEncounterScopeReturnsOnlyAuthorizedEncounter() {

        EncounterRepository repository =
                Mockito.mock(EncounterRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        EncounterService service =
                new EncounterService(
                        repository,
                        scopes
                );

        Encounter encounter =
                new Encounter();

        when(repository.findByIdAndPatientId(
                "encounter-1",
                "patient-1"
        )).thenReturn(List.of(encounter));

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/patient",
                                "launch/encounter",
                                "patient/Encounter.rs"
                        ),
                        "patient-1",
                        "encounter-1"
                );

        service.findAuthorizedEncounter(
                authentication,
                "encounter-1"
        );

        verify(repository).findByIdAndPatientId(
                "encounter-1",
                "patient-1"
        );
    }

    @Test
    void missingEncounterSearchScopeIsDenied() {

        EncounterRepository repository =
                Mockito.mock(EncounterRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        EncounterService service =
                new EncounterService(
                        repository,
                        scopes
                );

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/patient",
                                "launch/encounter",
                                "patient/Encounter.read"
                        ),
                        "patient-1",
                        "encounter-1"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> service.findAuthorizedEncounter(
                        authentication,
                        "encounter-1"
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void wrongEncounterIdIsDenied() {

        EncounterRepository repository =
                Mockito.mock(EncounterRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        EncounterService service =
                new EncounterService(
                        repository,
                        scopes
                );

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/encounter",
                                "patient/Encounter.rs"
                        ),
                        "patient-1",
                        "encounter-1"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> service.findAuthorizedEncounter(
                        authentication,
                        "encounter-2"
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void missingPatientContextIsDenied() {

        EncounterRepository repository =
                Mockito.mock(EncounterRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        EncounterService service =
                new EncounterService(
                        repository,
                        scopes
                );

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/encounter",
                                "patient/Encounter.rs"
                        ),
                        null,
                        "encounter-1"
                );

        assertThrows(
                AccessDeniedException.class,
                () -> service.findAuthorizedEncounter(
                        authentication,
                        "encounter-1"
                )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void missingEncounterContextIsDenied() {

        EncounterRepository repository =
                Mockito.mock(EncounterRepository.class);

        SmartScopeService scopes =
                new SmartScopeService();

        EncounterService service =
                new EncounterService(
                        repository,
                        scopes
                );

        JwtAuthenticationToken authentication =
                authentication(
                        List.of(
                                "openid",
                                "launch/patient",
                                "patient/Encounter.rs"
                        ),
                        "patient-1",
                        null
                );

        assertThrows(
                AccessDeniedException.class,
                () -> service.findAuthorizedEncounter(
                        authentication,
                        "encounter-1"
                )
        );

        verifyNoInteractions(repository);
    }

    private JwtAuthenticationToken authentication(
            List<String> scopes,
            String patient,
            String encounter) {

        Map<String, Object> claims =
                new java.util.HashMap<>();

        claims.put("scope", scopes);
        claims.put("sub", "smart-client");

        if (patient != null) {
            claims.put("patient", patient);
        }

        if (encounter != null) {
            claims.put("encounter", encounter);
        }

        Jwt jwt =
                new Jwt(
                        "test-token",
                        Instant.now(),
                        Instant.now().plusSeconds(300),
                        Map.of("alg", "none"),
                        claims
                );

        return new JwtAuthenticationToken(jwt);
    }
}
