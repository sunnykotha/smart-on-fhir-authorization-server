package smart_on_fhir_server.smart;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SmartLaunchController {

    private static final String AUTHORIZATION_ENDPOINT =
            "http://localhost:8080/oauth2/authorize";

    private static final String CLIENT_ID =
            "smart-client";

    private static final String REDIRECT_URI =
            "http://localhost:8080/callback";

    private final SecureRandom secureRandom =
            new SecureRandom();

    @GetMapping("/smart/launch")
    public String launch(
            @RequestParam String patient,
            @RequestParam(required = false) String encounter,
            @RequestParam(required = false) String code,
            HttpSession session) throws Exception {

        System.out.println(
                "SMART LAUNCH PATIENT = " + patient
        );

        System.out.println(
                "SMART LAUNCH ENCOUNTER = " + encounter
        );

        System.out.println(
                "SMART LAUNCH OBSERVATION CODE = " + code
        );

        /*
         * Generate PKCE code verifier.
         */
        byte[] verifierBytes = new byte[32];

        secureRandom.nextBytes(verifierBytes);

        String codeVerifier =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(verifierBytes);

        /*
         * Generate PKCE S256 code challenge.
         */
        byte[] hash =
                MessageDigest
                        .getInstance("SHA-256")
                        .digest(
                                codeVerifier.getBytes(
                                        StandardCharsets.US_ASCII
                                )
                        );

        String codeChallenge =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(hash);

        /*
         * Generate OAuth state.
         */
        byte[] stateBytes = new byte[32];

        secureRandom.nextBytes(stateBytes);

        String state =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(stateBytes);

        /*
         * Store values in the client session.
         */
        session.setAttribute(
                "SMART_LAUNCH_PATIENT",
                patient
        );

        if (encounter != null && !encounter.isBlank()) {
            session.setAttribute(
                    "SMART_LAUNCH_ENCOUNTER",
                    encounter
            );
        }

        session.setAttribute(
                "SMART_CODE_VERIFIER",
                codeVerifier
        );

        session.setAttribute(
                "SMART_STATE",
                state
        );

        String requestedScope =
                code != null && !code.isBlank()
                        ? "openid launch/patient patient/Observation.rs?code=" + code
                        : "openid launch/patient patient/*.read patient/*.rs";

        String authorizationUrl =
                AUTHORIZATION_ENDPOINT
                + "?response_type=code"
                + "&client_id="
                + encode(CLIENT_ID)
                + "&scope="
                + encode(requestedScope)
                + "&launch="
                + encode(patient)
                + "&patient="
                + encode(patient)
                + (encounter != null && !encounter.isBlank()
                        ? "&encounter=" + encode(encounter)
                        : "")
                + "&redirect_uri="
                + encode(REDIRECT_URI)
                + "&state="
                + encode(state)
                + "&code_challenge="
                + encode(codeChallenge)
                + "&code_challenge_method=S256";

        System.out.println(
                "SMART AUTHORIZATION REQUEST CREATED"
        );

        return "redirect:" + authorizationUrl;
    }

    private String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }
}
