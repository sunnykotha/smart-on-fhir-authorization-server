package smart_on_fhir_server.smart;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class ScopeAuthorizationService {

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
                    .anyMatch(granted ->
                            matches(granted, requiredScope));
        }

        String scope = rawScope.toString();

        if (scope.isBlank()) {
            return false;
        }

        return Arrays.stream(scope.split("\\s+"))
                .anyMatch(granted ->
                        matches(granted, requiredScope));
    }

    private boolean matches(
            String granted,
            String required) {

        if (granted.equals(required)) {
            return true;
        }

        if ("patient/*.read".equals(granted)
                && required.startsWith("patient/")
                && required.endsWith(".read")) {
            return true;
        }

        if ("patient/*.rs".equals(granted)
                && required.startsWith("patient/")
                && required.endsWith(".rs")) {
            return true;
        }

        if ("user/*.read".equals(granted)
                && required.startsWith("user/")
                && required.endsWith(".read")) {
            return true;
        }

        return false;
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

    public boolean hasPatientRead(
            JwtAuthenticationToken authentication,
            String resource) {

        return hasAnyScope(
                authentication,
                "patient/" + resource + ".read",
                "patient/*.read",
                "patient/" + resource + ".rs",
                "patient/*.rs"
        );
    }

    public boolean hasPatientSearch(
            JwtAuthenticationToken authentication,
            String resource) {

        return hasAnyScope(
                authentication,
                "patient/" + resource + ".rs",
                "patient/*.rs"
        );
    }

    public boolean hasUserRead(
            JwtAuthenticationToken authentication,
            String resource) {

        return hasAnyScope(
                authentication,
                "user/" + resource + ".read",
                "user/*.read"
        );
    }
}
