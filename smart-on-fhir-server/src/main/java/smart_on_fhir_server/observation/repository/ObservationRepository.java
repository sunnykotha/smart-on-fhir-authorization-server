package smart_on_fhir_server.observation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_on_fhir_server.observation.Observation;

public interface ObservationRepository
        extends JpaRepository<Observation, String> {

    List<Observation> findByPatientId(String patientId);

    List<Observation> findByPatientIdAndCode(
            String patientId,
            String code);
}
