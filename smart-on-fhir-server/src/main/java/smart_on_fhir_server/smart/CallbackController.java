package smart_on_fhir_server.smart;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
public class CallbackController {

    private static final String TOKEN_ENDPOINT =
            "http://localhost:8080/oauth2/token";

    private static final String CLIENT_ID =
            "smart-client";

    private static final String CLIENT_SECRET =
            "smart-secret";

    private static final String REDIRECT_URI =
            "http://localhost:8080/callback";

    private final RestClient restClient =
            RestClient.create();

    @GetMapping("/callback")
    public Object callback(
            @RequestParam String code,
            @RequestParam String state,
            HttpSession session) {

        System.out.println(
                "CALLBACK SESSION ID = " + session.getId()
        );

        Object storedState =
                session.getAttribute("SMART_STATE");

        System.out.println(
                "CALLBACK SMART_STATE = " + storedState
        );

        System.out.println(
                "CALLBACK STATE = " + state
        );

        /*
         * Validate OAuth state before using the
         * authorization code.
         */
        if (storedState == null) {

            return Map.of(
                    "error",
                    "OAuth state is missing from the client session."
            );
        }

        if (!storedState.equals(state)) {

            return Map.of(
                    "error",
                    "OAuth state mismatch."
            );
        }

        /*
         * Retrieve the PKCE verifier that was generated
         * when /smart/launch started.
         */
        Object verifier =
                session.getAttribute(
                        "SMART_CODE_VERIFIER"
                );

        if (verifier == null
                || verifier.toString().isBlank()) {

            return Map.of(
                    "error",
                    "PKCE code verifier is missing from the client session."
            );
        }

        String codeVerifier =
                verifier.toString();

        /*
         * Build the OAuth authorization-code token request.
         */
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add(
                "grant_type",
                "authorization_code"
        );

        form.add(
                "code",
                code
        );

        form.add(
                "redirect_uri",
                REDIRECT_URI
        );

        form.add(
                "client_id",
                CLIENT_ID
        );

        form.add(
                "code_verifier",
                codeVerifier
        );

        System.out.println(
                "CALLBACK: EXCHANGING AUTHORIZATION CODE"
        );

        try {

            Map<String, Object> tokenResponse =
                    restClient
                            .post()
                            .uri(TOKEN_ENDPOINT)
                            .headers(headers ->
                                    headers.setBasicAuth(
                                            CLIENT_ID,
                                            CLIENT_SECRET
                                    )
                            )
                            .contentType(
                                    MediaType.APPLICATION_FORM_URLENCODED
                            )
                            .body(form)
                            .retrieve()
                            .body(Map.class);

            /*
             * Authorization code and PKCE verifier are
             * single-use values. Remove them after the
             * exchange.
             */
            session.removeAttribute(
                    "SMART_STATE"
            );

            session.removeAttribute(
                    "SMART_CODE_VERIFIER"
            );

            System.out.println(
                    "CALLBACK: TOKEN EXCHANGE SUCCESSFUL"
            );

            return tokenResponse;

        } catch (Exception exception) {

            System.out.println(
                    "CALLBACK: TOKEN EXCHANGE FAILED = "
                    + exception.getMessage()
            );

            return Map.of(
                    "error",
                    "Token exchange failed",
                    "message",
                    exception.getMessage() == null
                            ? "Unknown token endpoint error"
                            : exception.getMessage()
            );
        }
    }
}
