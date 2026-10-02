package smart_on_fhir_server.smart;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class SmartScopeService {

    public boolean hasScope(
            JwtAuthenticationToken authentication,
            String requiredScope) {

        if (authentication == null || requiredScope == null) {
            return false;
        }

        Object rawScope =
                authentication.getToken()
                        .getClaims()
                        .get("scope");

        if (rawScope == null) {
            return false;
        }

        if (rawScope instanceof Collection<?> scopes) {
            return scopes.stream()
                    .map(Object::toString)
                    .anyMatch(requiredScope::equals);
        }

        String scope = rawScope.toString();

        if (scope.isBlank()) {
            return false;
        }

        return Arrays.stream(scope.split("\\s+"))
                .anyMatch(requiredScope::equals);
    }

    public SmartScope parseScope(String rawScope) {

        if (rawScope == null || rawScope.isBlank()) {
            throw new IllegalArgumentException("SMART scope cannot be blank");
        }

        String[] parts = rawScope.split("\\?", 2);

        String scopePart = parts[0];

        String[] scopeSegments = scopePart.split("/", 2);

        if (scopeSegments.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid SMART scope: " + rawScope
            );
        }

        String type = scopeSegments[0];

        String resourceAndPermission = scopeSegments[1];

        int permissionSeparator =
                resourceAndPermission.lastIndexOf(".");

        if (permissionSeparator <= 0
                || permissionSeparator == resourceAndPermission.length() - 1) {

            throw new IllegalArgumentException(
                    "Invalid SMART scope: " + rawScope
            );
        }

        String resource =
                resourceAndPermission.substring(
                        0,
                        permissionSeparator
                );

        String permissions =
                resourceAndPermission.substring(
                        permissionSeparator + 1
                );

        if (!"patient".equals(type)
                && !"user".equals(type)) {

            throw new IllegalArgumentException(
                    "Unsupported SMART scope type: " + type
            );
        }

        if (resource.isBlank()
                || permissions.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid SMART scope: " + rawScope
            );
        }

        Map<String, String> constraints =
                new java.util.HashMap<>();

        if (parts.length == 2
                && !parts[1].isBlank()) {

            for (String expression : parts[1].split("&")) {

                String[] constraint =
                        expression.split("=", 2);

                if (constraint.length != 2
                        || constraint[0].isBlank()
                        || constraint[1].isBlank()) {

                    throw new IllegalArgumentException(
                            "Invalid SMART search constraint: "
                            + expression
                    );
                }

                String name =
                        constraint[0].trim();

                String value =
                        constraint[1].trim();

                if (!"code".equals(name)) {
                    throw new IllegalArgumentException(
                            "Unsupported SMART search parameter: "
                            + name
                    );
                }

                constraints.put(name, value);
            }
        }

        return new SmartScope(
                type,
                resource,
                permissions,
                Map.copyOf(constraints)
        );
    }
    public SmartScope findPatientSearchScope(
            JwtAuthenticationToken authentication,
            String resourceType) {

        if (authentication == null
                || resourceType == null
                || resourceType.isBlank()) {
            return null;
        }

        Object rawScope =
                authentication.getToken()
                        .getClaims()
                        .get("scope");

        if (rawScope == null) {
            return null;
        }

        if (rawScope instanceof Collection<?> scopes) {

            for (Object value : scopes) {

                if (value == null) {
                    continue;
                }

                try {

                    SmartScope scope =
                            parseScope(value.toString());

                    if (scope.isPatientScope()
                            && scope.allowsSearch()
                            && (resourceType.equals(scope.resource())
                                || "*".equals(scope.resource()))) {

                        return scope;
                    }

                } catch (IllegalArgumentException ignored) {
                    /*
                     * OAuth/OIDC scopes such as "openid" and
                     * "launch/patient" are not resource scopes.
                     * They must not cause resource authorization
                     * to fail or grant access.
                     */
                }
            }

            return null;
        }

        for (String value :
                rawScope.toString().split("\\s+")) {

            try {

                SmartScope scope =
                        parseScope(value);

                if (scope.isPatientScope()
                        && scope.allowsSearch()
                        && (resourceType.equals(scope.resource())
                            || "*".equals(scope.resource()))) {

                    return scope;
                }

            } catch (IllegalArgumentException ignored) {
                /*
                 * Ignore non-resource OAuth/OIDC scopes.
                 */
            }
        }

        return null;
    }
    public boolean hasAnyScope(
            JwtAuthenticationToken authentication,
            String... requiredScopes) {

        if (requiredScopes == null) {
            return false;
        }

        return Arrays.stream(requiredScopes)
                .anyMatch(scope ->
                        hasScope(authentication, scope));
    }

    public boolean canReadPatient(
            JwtAuthenticationToken authentication) {

        return hasAnyScope(
                authentication,
                "patient/*.read",
                "patient/Patient.read",
                "patient/Patient.rs"
        );
    }

    public boolean canUserRead(
            JwtAuthenticationToken authentication) {

        return hasAnyScope(
                authentication,
                "user/*.read",
                "user/Patient.read",
                "user/Patient.rs"
        );
    }

    public String patientContext(
            JwtAuthenticationToken authentication) {

        if (authentication == null) {
            return null;
        }

        String patient =
                authentication.getToken()
                        .getClaimAsString("patient");

        if (patient == null || patient.isBlank()) {
            return null;
        }

        return patient;
    }

    public boolean hasPatientReadScope(
            JwtAuthenticationToken authentication,
            String resourceType) {

        return hasAnyScope(
                authentication,
                "patient/*.read",
                "patient/" + resourceType + ".read",
                "patient/" + resourceType + ".rs"
        );
    }

    public boolean hasUserReadScope(
            JwtAuthenticationToken authentication,
            String resourceType) {

        return hasAnyScope(
                authentication,
                "user/*.read",
                "user/" + resourceType + ".read",
                "user/" + resourceType + ".rs"
        );
    }
}

