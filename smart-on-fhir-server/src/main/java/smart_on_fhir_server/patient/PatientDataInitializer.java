package smart_on_fhir_server.patient;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import smart_on_fhir_server.patient.repository.PatientRepository;

@Configuration
public class PatientDataInitializer {

    @Bean
    CommandLineRunner initializePatients(
            PatientRepository patientRepository) {

        return args -> {

            patientRepository.save(
                    new Patient(
                            "patient-1",
                            "Doe",
                            "John"
                    )
            );

            patientRepository.save(
                    new Patient(
                            "patient-2",
                            "Smith",
                            "Jane"
                    )
            );

            System.out.println(
                    "DETERMINISTIC PATIENTS INITIALIZED"
            );

            System.out.println(
                    "patient-1 = John Doe"
            );

            System.out.println(
                    "patient-2 = Jane Smith"
            );
        };
    }
}
