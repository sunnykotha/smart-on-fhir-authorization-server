package smart_on_fhir_server.patient.controller;

import java.util.List;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smart_on_fhir_server.patient.Patient;
import smart_on_fhir_server.patient.service.PatientService;

@RestController
@RequestMapping("/fhir")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/Patient")
    public List<Patient> getPatients(
            @RequestParam(required = false) String id,
            JwtAuthenticationToken authentication) {

        System.out.println(
                "FHIR PATIENT SEARCH id = " + id
        );

        System.out.println(
                "FHIR PATIENT CONTEXT = "
                        + authentication.getToken()
                                .getClaimAsString("patient")
        );

        System.out.println(
                "FHIR SCOPE = "
                        + authentication.getToken()
                                .getClaimAsString("scope")
        );

        return patientService.searchPatients(
                authentication,
                id
        );
    }
}