package smart_on_fhir_server.patient.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import smart_on_fhir_server.patient.Patient;
import smart_on_fhir_server.patient.repository.PatientRepository;
import smart_on_fhir_server.smart.SmartScopeService;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private SmartScopeService smartScopeService;

    @InjectMocks
    private PatientService patientService;

    @Test
    void userReadScopeAllowsQueryWithoutPatientContext() {

        Patient patient1 =
                new Patient("patient-1", "Doe", "John");

        Patient patient2 =
                new Patient("patient-2", "Smith", "Jane");

        when(smartScopeService.canUserRead(any()))
                .thenReturn(true);

        when(patientRepository.findAll())
                .thenReturn(List.of(patient1, patient2));

        JwtAuthenticationToken authentication =
                authenticationWithScope("user/*.read");

        List<Patient> result =
                patientService.searchPatients(
                        authentication,
                        null);

        assertEquals(2, result.size());

        verify(patientRepository).findAll();

        verify(patientRepository, never())
                .findById(anyString());
    }

    @Test
    void patientReadScopeRemainsRestrictedToPatientContext() {

        Patient patient1 =
                new Patient("patient-1", "Doe", "John");

        when(smartScopeService.canUserRead(any()))
                .thenReturn(false);

        when(smartScopeService.canReadPatient(any()))
                .thenReturn(true);

        when(smartScopeService.patientContext(any()))
                .thenReturn("patient-1");

        when(patientRepository.findById("patient-1"))
                .thenReturn(java.util.Optional.of(patient1));

        JwtAuthenticationToken authentication =
                authenticationWithScope("patient/*.read");

        List<Patient> result =
                patientService.searchPatients(
                        authentication,
                        null);

        assertEquals(1, result.size());
        assertEquals("patient-1", result.get(0).getId());

        verify(patientRepository)
                .findById("patient-1");

        verify(patientRepository, never())
                .findAll();
    }

    private JwtAuthenticationToken authenticationWithScope(
            String scope) {

        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .claim("sub", "smartuser")
                        .claim("scope", scope)
                        .build();

        return new JwtAuthenticationToken(jwt);
    }
}
