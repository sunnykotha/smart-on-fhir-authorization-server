package smart_on_fhir_server.encounter;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import smart_on_fhir_server.encounter.repository.EncounterRepository;

@Configuration
public class EncounterDataInitializer {

    @Bean
    CommandLineRunner initializeEncounters(
            EncounterRepository encounterRepository) {

        return args -> {

            encounterRepository.save(
                    new Encounter(
                            "encounter-1",
                            "patient-1",
                            "in-progress"
                    )
            );

            System.out.println(
                    "DETERMINISTIC ENCOUNTER INITIALIZED"
            );

            System.out.println(
                    "encounter-1 -> patient-1"
            );
        };
    }
}
