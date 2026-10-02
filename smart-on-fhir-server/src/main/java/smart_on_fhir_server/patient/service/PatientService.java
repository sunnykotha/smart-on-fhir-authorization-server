package smart_on_fhir_server.patient.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import smart_on_fhir_server.patient.Patient;
import smart_on_fhir_server.patient.repository.PatientRepository;
import smart_on_fhir_server.smart.SmartScopeService;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final SmartScopeService smartScopeService;

    public PatientService(
            PatientRepository patientRepository,
            SmartScopeService smartScopeService) {

        this.patientRepository = patientRepository;
        this.smartScopeService = smartScopeService;
    }

    public List<Patient> searchPatients(
            JwtAuthenticationToken authentication,
            String id) {

        /*
         * User-level scope:
         * user/*.read allows querying Patient resources without
         * applying a patient launch-context restriction.
         */
        if (smartScopeService.canUserRead(authentication)) {

            if (id != null && !id.isBlank()) {
                return patientRepository.findAllById(id);
            }

            return patientRepository.findAll();
        }

        /*
         * Patient-level scope:
         * patient/*.read and Patient-specific patient scopes are
         * restricted to the SMART patient context.
         */
        if (!smartScopeService.canReadPatient(authentication)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Required SMART patient read scope"
            );
        }

        String patientContext =
                smartScopeService.patientContext(authentication);

        if (patientContext == null
                || patientContext.isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Missing SMART patient context"
            );
        }

        if (id != null
                && !id.isBlank()
                && !patientContext.equals(id)) {

            throw new AccessDeniedException(
                    "Requested patient is outside SMART context"
            );
        }

        return patientRepository
                .findById(patientContext)
                .map(List::of)
                .orElseGet(List::of);
    }
}
