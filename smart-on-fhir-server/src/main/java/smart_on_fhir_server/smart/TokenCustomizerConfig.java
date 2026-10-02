package smart_on_fhir_server.smart;

import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

@Configuration
public class TokenCustomizerConfig {

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {

        return context -> {

            if (!OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                return;
            }

            OAuth2Authorization authorization =
                    context.getAuthorization();

            if (authorization == null) {
                System.out.println(
                        "TOKEN CUSTOMIZER AUTHORIZATION = null"
                );
                return;
            }

            OAuth2AuthorizationRequest authorizationRequest =
                    authorization.getAttribute(
                            OAuth2AuthorizationRequest.class.getName()
                    );

            if (authorizationRequest == null) {
                System.out.println(
                        "TOKEN CUSTOMIZER AUTHORIZATION REQUEST = null"
                );
                System.out.println(
                        "TOKEN CUSTOMIZER AUTHORIZATION ATTRIBUTES = "
                        + authorization.getAttributes()
                );
                return;
            }

            Map<String, Object> additionalParameters =
                    authorizationRequest.getAdditionalParameters();

            System.out.println(
                    "TOKEN CUSTOMIZER ADDITIONAL PARAMETERS = "
                    + additionalParameters
            );

            Object patient =
                    additionalParameters.get("SMART_LAUNCH_PATIENT");

            if (patient == null) {
                patient =
                        additionalParameters.get("patient");
            }

            System.out.println(
                    "TOKEN CUSTOMIZER PATIENT = "
                    + patient
            );

            if (patient != null
                    && !patient.toString().isBlank()) {

                String patientId =
                        patient.toString();

                context.getClaims()
                        .claim("patient", patientId);

                System.out.println(
                        "TOKEN CUSTOMIZER: PATIENT CLAIM ADDED = "
                        + patientId
                );
            }

            Object encounter =
                    additionalParameters.get("SMART_LAUNCH_ENCOUNTER");

            if (encounter == null) {
                encounter =
                        additionalParameters.get("encounter");
            }

            System.out.println(
                    "TOKEN CUSTOMIZER ENCOUNTER = "
                    + encounter
            );

            if (encounter != null
                    && !encounter.toString().isBlank()) {

                String encounterId =
                        encounter.toString();

                context.getClaims()
                        .claim("encounter", encounterId);

                System.out.println(
                        "TOKEN CUSTOMIZER: ENCOUNTER CLAIM ADDED = "
                        + encounterId
                );
            }
        };
    }
}
