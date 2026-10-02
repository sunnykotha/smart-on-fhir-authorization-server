package smart_on_fhir_server.smart;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SmartConfigurationController {

    @GetMapping("/.well-known/smart-configuration")
    public Map<String, Object> smartConfiguration() {

        Map<String, Object> config = new LinkedHashMap<>();

        config.put("authorization_endpoint",
                "http://localhost:8080/oauth2/authorize");

        config.put("token_endpoint",
                "http://localhost:8080/oauth2/token");

        config.put("introspection_endpoint",
                "http://localhost:8080/oauth2/introspect");

        config.put("revocation_endpoint",
                "http://localhost:8080/oauth2/revoke");

        config.put("capabilities", List.of(
                "launch-ehr",
                "launch-standalone",
                "client-public",
                "client-confidential-symmetric",
                "sso-openid-connect",
                "context-ehr-patient",
                "context-ehr-encounter",
                "context-standalone-patient",
                "context-standalone-encounter",
                "permission-patient",
                "permission-user",
                "permission-offline"
        ));

        config.put("scopes_supported", List.of(
                "openid",
                "launch",
                "launch/patient",
                "launch/encounter",
                "patient/*.read",
                "patient/*.rs",
                "patient/Observation.rs",
                "patient/Encounter.rs",
                "patient/Observation.rs?code=heart-rate",
                "user/*.read",
                "offline_access"
        ));

        config.put("response_types_supported", List.of("code"));

        config.put("code_challenge_methods_supported", List.of("S256"));

        return config;
    }
}


