package smart_on_fhir_server.patient.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import smart_on_fhir_server.patient.Patient;

public interface PatientRepository
        extends JpaRepository<Patient, String> {

    List<Patient> findAllById(String id);
}