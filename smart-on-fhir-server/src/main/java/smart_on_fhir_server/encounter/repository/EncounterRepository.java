package smart_on_fhir_server.encounter.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_on_fhir_server.encounter.Encounter;

public interface EncounterRepository
        extends JpaRepository<Encounter, String> {

    List<Encounter> findByPatientId(String patientId);

    List<Encounter> findByIdAndPatientId(
            String id,
            String patientId);
}
