package smart_on_fhir_server.observation.service;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

import smart_on_fhir_server.observation.Observation;
import smart_on_fhir_server.observation.repository.ObservationRepository;
import smart_on_fhir_server.smart.SmartScope;
import smart_on_fhir_server.smart.SmartScopeService;

@Service
public class ObservationService {

    private final ObservationRepository repository;
    private final SmartScopeService scopes;

    public ObservationService(
            ObservationRepository repository,
            SmartScopeService scopes) {

        this.repository = repository;
        this.scopes = scopes;
    }

    public List<Observation> search(
            JwtAuthenticationToken authentication,
            String code) {

        /*
         * Find the actual patient Observation search scope
         * from the signed access token.
         */
        SmartScope observationScope =
                scopes.findPatientSearchScope(
                        authentication,
                        "Observation"
                );

        if (observationScope == null) {

            throw new AccessDeniedException(
                    "Required SMART scope: patient/Observation.rs"
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
         * An authorization scope may contain a search constraint.
         *
         * Example:
         *
         * patient/Observation.rs?code=heart-rate
         *
         * If such a constraint exists, the caller cannot
         * remove it or replace it with another code.
         */
        String authorizedCode =
                observationScope.constraint("code");

        if (authorizedCode != null
                && !authorizedCode.isBlank()) {

            if (code != null
                    && !code.isBlank()
                    && !authorizedCode.equals(code)) {

                throw new AccessDeniedException(
                        "Requested code conflicts with SMART scope constraint"
                );
            }

            /*
             * The authorization constraint becomes part of
             * the database query itself.
             */
            return repository.findByPatientIdAndCode(
                    patientId,
                    authorizedCode
            );
        }

        /*
         * No authorization-level code constraint exists.
         *
         * The normal FHIR code search parameter may be used,
         * but it is always combined with the JWT patient
         * security predicate.
         */
        if (code == null || code.isBlank()) {

            return repository.findByPatientId(patientId);
        }

        return repository.findByPatientIdAndCode(
                patientId,
                code
        );
    }
}