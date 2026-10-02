package smart_on_fhir_server.encounter.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import smart_on_fhir_server.encounter.Encounter;
import smart_on_fhir_server.encounter.repository.EncounterRepository;
import smart_on_fhir_server.smart.SmartScope;
import smart_on_fhir_server.smart.SmartScopeService;

@Service
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final SmartScopeService scopes;

    public EncounterService(
            EncounterRepository encounterRepository,
            SmartScopeService scopes) {

        this.encounterRepository = encounterRepository;
        this.scopes = scopes;
    }

    public List<Encounter> findAuthorizedEncounter(
            JwtAuthenticationToken authentication,
            String requestedEncounterId) {

        /*
         * Find the actual patient Encounter search scope
         * from the signed access token.
         */
        SmartScope encounterScope =
                scopes.findPatientSearchScope(
                        authentication,
                        "Encounter"
                );

        if (encounterScope == null) {

            throw new AccessDeniedException(
                    "Required SMART scope: patient/Encounter.rs"
            );
        }

        /*
         * Patient context comes ONLY from the signed JWT.
         */
        String patientId =
                authentication.getToken()
                        .getClaimAsString("patient");

        if (patientId == null || patientId.isBlank()) {

            throw new AccessDeniedException(
                    "Missing SMART patient context"
            );
        }

        /*
         * Encounter context comes ONLY from the signed JWT.
         */
        String authorizedEncounterId =
                authentication.getToken()
                        .getClaimAsString("encounter");

        if (authorizedEncounterId == null
                || authorizedEncounterId.isBlank()) {

            throw new AccessDeniedException(
                    "Missing SMART encounter context"
            );
        }

        /*
         * If the caller requests a specific encounter,
         * it must match the encounter authorized by the
         * signed SMART launch context.
         */
        if (requestedEncounterId != null
                && !requestedEncounterId.isBlank()
                && !authorizedEncounterId.equals(requestedEncounterId)) {

            throw new AccessDeniedException(
                    "Requested encounter is outside SMART context"
            );
        }

        /*
         * The database query is restricted by BOTH:
         *
         * 1. the authorized encounter from the JWT
         * 2. the authorized patient from the JWT
         *
         * This prevents access to an encounter belonging
         * to another patient.
         */
        return encounterRepository.findByIdAndPatientId(
                authorizedEncounterId,
                patientId
        );
    }
}
