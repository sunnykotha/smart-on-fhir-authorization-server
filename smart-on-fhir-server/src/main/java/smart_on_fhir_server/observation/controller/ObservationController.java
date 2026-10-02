package smart_on_fhir_server.observation.controller;

import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smart_on_fhir_server.observation.Observation;
import smart_on_fhir_server.observation.service.ObservationService;

@RestController
@RequestMapping("/fhir")
public class ObservationController {

    private final ObservationService observationService;

    public ObservationController(ObservationService observationService) {
        this.observationService = observationService;
    }

    @GetMapping("/Observation")
    public List<Observation> search(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String patient,
            @RequestParam(required = false) String _include,
            @RequestParam(required = false) String _revinclude,
            JwtAuthenticationToken authentication) {

        /*
         * Patient-scoped SMART access:
         * the caller may request only the patient contained
         * in the signed access-token context.
         */
        if (patient != null && !patient.isBlank()) {

            String tokenPatient =
                    authentication.getToken()
                            .getClaimAsString("patient");

            if (tokenPatient == null
                    || !patient.equals(tokenPatient)) {

                throw new AccessDeniedException(
                        "Patient search parameter conflicts with SMART patient context"
                );
            }
        }

        /*
         * Relationship expansion is denied until every
         * returned resource can be independently authorized.
         */
        if ((_include != null && !_include.isBlank())
                || (_revinclude != null && !_revinclude.isBlank())) {

            throw new AccessDeniedException(
                    "_include and _revinclude are not permitted"
            );
        }

        return observationService.search(
                authentication,
                code
        );
    }
}
