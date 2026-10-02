package smart_on_fhir_server.observation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import smart_on_fhir_server.observation.repository.ObservationRepository;

@Configuration
public class ObservationDataInitializer {

    @Bean
    CommandLineRunner initializeObservations(
            ObservationRepository repository) {

        return args -> {

            if (repository.count() > 0) {
                return;
            }

            repository.save(
                    new Observation(
                            "observation-1",
                            "patient-1",
                            "heart-rate",
                            "72",
                            "final"
                    )
            );

            repository.save(
                    new Observation(
                            "observation-2",
                            "patient-2",
                            "heart-rate",
                            "80",
                            "final"
                    )
            );

            repository.save(
                    new Observation(
                            "observation-3",
                            "patient-2",
                            "blood-pressure",
                            "120/80",
                            "final"
                    )
            );

            System.out.println(
                    "OBSERVATIONS INITIALIZED"
            );
        };
    }
}
