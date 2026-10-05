package smart_on_fhir_server;

import java.util.UUID;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class ClientConfig {

    @Bean
    RegisteredClientRepository registeredClientRepository(
            PasswordEncoder passwordEncoder) {

        RegisteredClient smartClient =
                RegisteredClient.withId(
                        UUID.randomUUID().toString()
                )
                .clientId("smart-client")
                .clientSecret(
                        passwordEncoder.encode("smart-secret")
                )
                .clientAuthenticationMethod(
                        ClientAuthenticationMethod.CLIENT_SECRET_BASIC
                )
                .authorizationGrantType(
                        AuthorizationGrantType.AUTHORIZATION_CODE
                )
                .authorizationGrantType(
                        AuthorizationGrantType.REFRESH_TOKEN
                )
                .redirectUri(
                        "http://localhost:8081/callback"
                )
                .scope("openid")
                .scope("launch")
                .scope("launch/patient")
                .scope("launch/encounter")
                .scope("patient/*.read")
                .scope("patient/*.rs")
                .scope("patient/Observation.rs")
                .scope("patient/Encounter.rs")
                .scope("patient/Observation.rs?code=heart-rate")
                .scope("user/*.read")
                .scope("offline_access")
                .clientSettings(
                        ClientSettings.builder()
                                .requireProofKey(true)
                                .requireAuthorizationConsent(false)
                                .build()
                )
                .tokenSettings(
                        TokenSettings.builder()
                                .reuseRefreshTokens(false)
                                .build()
                )
                .build();

        return new InMemoryRegisteredClientRepository(
                smartClient
        );
    }
}
