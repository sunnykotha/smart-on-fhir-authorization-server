package smart_on_fhir_server.smart;

public record SmartAuthorizationContext(
        PatientContext patientContext,
        boolean patientScoped,
        boolean userScoped
) {
}
