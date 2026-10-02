package smart_on_fhir_server.observation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "observations")
public class Observation {

    @Id
    private String id;

    private String patientId;

    private String code;

    @Column(name = "observation_value")
    private String value;

    private String status;

    public Observation() {
    }

    public Observation(
            String id,
            String patientId,
            String code,
            String value,
            String status) {

        this.id = id;
        this.patientId = patientId;
        this.code = code;
        this.value = value;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getStatus() {
        return status;
    }
}