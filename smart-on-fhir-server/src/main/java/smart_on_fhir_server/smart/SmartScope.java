package smart_on_fhir_server.smart;

import java.util.Map;

public record SmartScope(
        String type,
        String resource,
        String permissions,
        Map<String, String> constraints
) {

    public boolean isPatientScope() {
        return "patient".equals(type);
    }

    public boolean isUserScope() {
        return "user".equals(type);
    }

    public boolean allowsRead() {
        return permissions.contains("r");
    }

    public boolean allowsSearch() {
        return permissions.contains("s");
    }

    public boolean allowsCreate() {
        return permissions.contains("c");
    }

    public boolean allowsUpdate() {
        return permissions.contains("u");
    }

    public boolean allowsDelete() {
        return permissions.contains("d");
    }

    public boolean hasConstraint(String name) {
        return constraints != null
                && constraints.containsKey(name);
    }

    public String constraint(String name) {
        if (constraints == null) {
            return null;
        }

        return constraints.get(name);
    }
}
