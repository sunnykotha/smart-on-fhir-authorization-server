package smart_on_fhir_server.encounter;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "encounters")
public class Encounter {

    @Id
    private String id;

    private String patientId;

    private String status;

    public Encounter() {
    }

    public Encounter(
            String id,
            String patientId,
            String status) {

        this.id = id;
        this.patientId = patientId;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
