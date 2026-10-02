package smart_on_fhir_client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class CallbackController {

    @Value("${smart.discovery-url}")
    private String discoveryUrl;

    @Value("${smart.client-id}")
    private String clientId;

    @Value("${smart.client-secret}")
    private String clientSecret;

    @Value("${smart.redirect-uri}")
    private String redirectUri;

    @Value("${smart.fhir-base-url}")
    private String fhirBaseUrl;

    @Value("${smart.launch-patient}")
    private String launchPatient;

    @Value("${smart.launch-encounter}")
    private String launchEncounter;

    private final SecureRandom secureRandom =
            new SecureRandom();

    private final ObjectMapper objectMapper =
            new ObjectMapper();


    @GetMapping("/")
    public String startAuthorization(
            HttpSession session) throws Exception {

        System.out.println("START SESSION ID = " + session.getId());

        RestTemplate restTemplate =
                new RestTemplate();

        ResponseEntity<String> discoveryResult =
                restTemplate.getForEntity(
                        discoveryUrl,
                        String.class
                );

        String discoveryResponse =
                discoveryResult.getBody();

        JsonNode discovery =
                objectMapper.readTree(
                        discoveryResponse
                );

        String authorizationEndpoint =
                discovery
                        .get("authorization_endpoint")
                        .asText();

        byte[] verifierBytes =
                new byte[32];

        secureRandom.nextBytes(
                verifierBytes
        );

        String codeVerifier =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                verifierBytes
                        );

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
                        .encodeToString(
                                hash
                        );

        byte[] stateBytes =
                new byte[32];

        secureRandom.nextBytes(
                stateBytes
        );

        String state =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                stateBytes
                        );

        session.setAttribute(
                "SMART_CODE_VERIFIER",
                codeVerifier
        );

        session.setAttribute(
                "SMART_STATE",
                state
        );

        String authorizationUrl =
                authorizationEndpoint
                + "?response_type=code"
                + "&client_id="
                + java.net.URLEncoder.encode(
                        clientId,
                        StandardCharsets.UTF_8
                )
                + "&scope="
                + java.net.URLEncoder.encode(
                        "openid launch/patient launch/encounter offline_access user/*.read",
                        StandardCharsets.UTF_8
                )
                + "&patient="
                + java.net.URLEncoder.encode(
                        launchPatient,
                        StandardCharsets.UTF_8
                )
                + "&encounter="
                + java.net.URLEncoder.encode(
                        launchEncounter,
                        StandardCharsets.UTF_8
                )
                + "&redirect_uri="
                + java.net.URLEncoder.encode(
                        redirectUri,
                        StandardCharsets.UTF_8
                )
                + "&state="
                + java.net.URLEncoder.encode(
                        state,
                        StandardCharsets.UTF_8
                )
                + "&code_challenge="
                + java.net.URLEncoder.encode(
                        codeChallenge,
                        StandardCharsets.UTF_8
                )
                + "&code_challenge_method=S256";

        return """
                <html>
                <body>

                    <h2>SMART on FHIR Client</h2>

                    <p>
                        SMART Discovery:
                        <b>%s</b>
                    </p>

                    <p>
                        Authorization Endpoint:
                        <b>%s</b>
                    </p>

                    <p>
                        Requested scopes:
                        <b>openid launch/patient launch/encounter offline_access user/*.read</b>
                    </p>

                    <p>
                        Launch patient:
                        <b>%s</b>
                    </p>

                    <p>
                        PKCE:
                        <b>S256</b>
                    </p>

                    <a href="%s">
                        Login with SMART Authorization Server
                    </a>

                </body>
                </html>
                """.formatted(
                        discoveryUrl,
                        authorizationEndpoint,
                        launchPatient,
                        authorizationUrl
                );
    }


    @GetMapping("/launch")
    public String standaloneLaunch(
            @RequestParam String patient,
            HttpSession session) throws Exception {

        System.out.println(
                "STANDALONE LAUNCH PATIENT = "
                + patient
        );

        System.out.println(
                "STANDALONE START SESSION ID = "
                + session.getId()
        );

        RestTemplate restTemplate =
                new RestTemplate();

        ResponseEntity<String> discoveryResult =
                restTemplate.getForEntity(
                        discoveryUrl,
                        String.class
                );

        JsonNode discovery =
                objectMapper.readTree(
                        discoveryResult.getBody()
                );

        String authorizationEndpoint =
                discovery
                        .get("authorization_endpoint")
                        .asText();

        byte[] verifierBytes =
                new byte[32];

        secureRandom.nextBytes(
                verifierBytes
        );

        String codeVerifier =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                verifierBytes
                        );

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
                        .encodeToString(
                                hash
                        );

        byte[] stateBytes =
                new byte[32];

        secureRandom.nextBytes(
                stateBytes
        );

        String state =
                Base64.getUrlEncoder()
                        .withoutPadding()
                        .encodeToString(
                                stateBytes
                        );

        session.setAttribute(
                "SMART_CODE_VERIFIER",
                codeVerifier
        );

        session.setAttribute(
                "SMART_STATE",
                state
        );

        session.setAttribute(
                "SMART_LAUNCH_PATIENT",
                patient
        );

        session.setAttribute(
                "SMART_LAUNCH_ENCOUNTER",
                launchEncounter
        );

        String authorizationUrl =
                authorizationEndpoint
                + "?response_type=code"
                + "&client_id="
                + java.net.URLEncoder.encode(
                        clientId,
                        StandardCharsets.UTF_8
                )
                + "&scope="
                + java.net.URLEncoder.encode(
                        "openid launch/patient launch/encounter offline_access user/*.read",
                        StandardCharsets.UTF_8
                )
                + "&patient="
                + java.net.URLEncoder.encode(
                        patient,
                        StandardCharsets.UTF_8
                )
                + "&encounter="
                + java.net.URLEncoder.encode(
                        launchEncounter,
                        StandardCharsets.UTF_8
                )
                + "&redirect_uri="
                + java.net.URLEncoder.encode(
                        redirectUri,
                        StandardCharsets.UTF_8
                )
                + "&state="
                + java.net.URLEncoder.encode(
                        state,
                        StandardCharsets.UTF_8
                )
                + "&code_challenge="
                + java.net.URLEncoder.encode(
                        codeChallenge,
                        StandardCharsets.UTF_8
                )
                + "&code_challenge_method=S256";

        System.out.println(
                "STANDALONE AUTHORIZATION REQUEST CREATED"
        );

        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/callback")
    public String callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(required = false) String error_description,
            HttpSession session) {

        System.out.println("CALLBACK SESSION ID = " + session.getId());
        System.out.println("CALLBACK SMART_STATE = " + session.getAttribute("SMART_STATE"));
        System.out.println("CALLBACK STATE = " + state);

        if (error != null) {

            return """
                    <html>
                    <body>
                        <h2>OAuth Error</h2>
                        <p><b>%s</b></p>
                        <p>%s</p>
                    </body>
                    </html>
                    """.formatted(
                            escapeHtml(error),
                            escapeHtml(error_description)
                    );
        }

        if (code == null) {

            return """
                    <html>
                    <body>
                        <h2>Error</h2>
                        <p>No authorization code received.</p>
                    </body>
                    </html>
                    """;
        }

        String expectedState =
                (String) session.getAttribute(
                        "SMART_STATE"
                );

        if (expectedState == null) {

            return """
                    <html>
                    <body>
                        <h2>OAuth Error</h2>
                        <p>OAuth state is missing from the client session.</p>
                    </body>
                    </html>
                    """;
        }

        if (state == null
                || !expectedState.equals(state)) {

            return """
                    <html>
                    <body>
                        <h2>OAuth Error</h2>
                        <p>Invalid OAuth state.</p>
                    </body>
                    </html>
                    """;
        }

        String codeVerifier =
                (String) session.getAttribute(
                        "SMART_CODE_VERIFIER"
                );

        if (codeVerifier == null) {

            return """
                    <html>
                    <body>
                        <h2>OAuth Error</h2>
                        <p>PKCE code verifier is missing.</p>
                    </body>
                    </html>
                    """;
        }

        try {

            RestTemplate restTemplate =
                    new RestTemplate();

            ResponseEntity<String> discoveryResult =
                    restTemplate.getForEntity(
                            discoveryUrl,
                            String.class
                    );

            JsonNode discovery =
                    objectMapper.readTree(
                            discoveryResult.getBody()
                    );

            String tokenEndpoint =
                    discovery
                            .get("token_endpoint")
                            .asText();

            HttpHeaders tokenHeaders =
                    new HttpHeaders();

            tokenHeaders.setContentType(
                    MediaType.APPLICATION_FORM_URLENCODED
            );

            tokenHeaders.setBasicAuth(
                    clientId,
                    clientSecret
            );

            MultiValueMap<String, String> tokenBody =
                    new LinkedMultiValueMap<>();

            tokenBody.add(
                    "grant_type",
                    "authorization_code"
            );

            tokenBody.add(
                    "code",
                    code
            );

            tokenBody.add(
                    "redirect_uri",
                    redirectUri
            );

            tokenBody.add(
                    "client_id",
                    clientId
            );

            tokenBody.add(
                    "code_verifier",
                    codeVerifier
            );

            HttpEntity<MultiValueMap<String, String>>
                    tokenRequest =
                    new HttpEntity<>(
                            tokenBody,
                            tokenHeaders
                    );

            ResponseEntity<String> tokenResult =
                    restTemplate.postForEntity(
                            tokenEndpoint,
                            tokenRequest,
                            String.class
                    );

            String tokenResponse =
                    tokenResult.getBody();

            System.out.println(
                    "TOKEN ENDPOINT = "
                    + tokenEndpoint
            );

            System.out.println(
                    "TOKEN HTTP STATUS = "
                    + tokenResult.getStatusCode()
            );

            String accessToken =
                    extractAccessToken(
                            tokenResponse
                    );

            if (accessToken != null
                    && !accessToken.isBlank()) {
                session.setAttribute(
                        "SMART_ACCESS_TOKEN",
                        accessToken
                );
            }

            JsonNode tokenJson =
                    objectMapper.readTree(tokenResponse);

            JsonNode refreshTokenNode =
                    tokenJson.get("refresh_token");

            if (refreshTokenNode != null
                    && !refreshTokenNode.isNull()
                    && !refreshTokenNode.asText().isBlank()) {

                session.setAttribute(
                        "SMART_REFRESH_TOKEN",
                        refreshTokenNode.asText()
                );
            }

            System.out.println(
                    "REFRESH TOKEN RECEIVED = "
                    + hasRefreshToken(tokenResponse)
            );

            System.out.println(
                    "ACCESS TOKEN RECEIVED = "
                    + (accessToken != null)
            );

            if (accessToken == null) {

                return """
                        <html>
                        <body>
                            <h2>Token Error</h2>
                            <pre>%s</pre>
                        </body>
                        </html>
                        """.formatted(
                                escapeHtml(
                                        tokenResponse
                                )
                        );
            }

            String fhirPatientEndpoint =
                    fhirBaseUrl + "/fhir/Encounter";

            HttpHeaders fhirHeaders =
                    new HttpHeaders();

            fhirHeaders.setBearerAuth(
                    accessToken
            );

            fhirHeaders.setAccept(
                    List.of(
                            MediaType.APPLICATION_JSON
                    )
            );

            HttpEntity<Void> fhirRequest =
                    new HttpEntity<>(
                            fhirHeaders
                    );

            ResponseEntity<String> fhirResult =
                    restTemplate.exchange(
                            fhirPatientEndpoint,
                            HttpMethod.GET,
                            fhirRequest,
                            String.class
                    );

            String patientResponse =
                    fhirResult.getBody();

            System.out.println(
                    "FHIR ENDPOINT = "
                    + fhirPatientEndpoint
            );

            System.out.println(
                    "FHIR HTTP STATUS = "
                    + fhirResult.getStatusCode()
            );

            session.removeAttribute(
                    "SMART_CODE_VERIFIER"
            );

            session.removeAttribute(
                    "SMART_STATE"
            );

            return """
                    <html>
                    <body>

                        <h1>SMART on FHIR Success</h1>

                        <h2>SMART Discovery</h2>

                        <p>
                            <b>%s</b>
                        </p>

                        <h2>Token Endpoint</h2>

                        <p>
                            <b>%s</b>
                        </p>

                        <h2>Requested Scope</h2>

                        <p>
                            <b>openid launch/patient launch/encounter offline_access user/*.read</b>
                        </p>

                        <h2>Launch Patient</h2>

                        <p>
                            <b>%s</b>
                        </p>

                        <h2>FHIR HTTP Status</h2>

                        <p>
                            <b>%s</b>
                        </p>

                        <h2>FHIR Patient Response</h2>

                        <pre>%s</pre>

                    </body>
                    </html>
                    """.formatted(
                            discoveryUrl,
                            tokenEndpoint,
                            launchPatient,
                            fhirResult.getStatusCode(),
                            escapeHtml(
                                    patientResponse
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return """
                    <html>
                    <body>
                        <h2>FHIR Request Failed</h2>
                        <pre>%s</pre>
                    </body>
                    </html>
                    """.formatted(
                            escapeHtml(
                                    e.getMessage()
                            )
                    );
        }
    }



    @GetMapping("/introspect-test")
    public String introspectTest(
            HttpSession session) throws Exception {

        RestTemplate restTemplate =
                new RestTemplate();

        String accessToken =
                (String) session.getAttribute(
                        "SMART_ACCESS_TOKEN"
                );

        if (accessToken == null
                || accessToken.isBlank()) {

            return "No access token in session. Run SMART authorization first.";
        }

        JsonNode discovery =
                objectMapper.readTree(
                        restTemplate.getForObject(
                                discoveryUrl,
                                String.class
                        )
                );

        String introspectionEndpoint =
                discovery
                        .get("introspection_endpoint")
                        .asText();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        headers.setBasicAuth(
                clientId,
                clientSecret
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add(
                "token",
                accessToken
        );

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<String> result =
                restTemplate.postForEntity(
                        introspectionEndpoint,
                        request,
                        String.class
                );

        return """
                <html>
                <body>
                <h1>Token Introspection Test</h1>
                <p>Token HTTP Status: %s</p>
                <pre>%s</pre>
                </body>
                </html>
                """.formatted(
                        result.getStatusCode(),
                        escapeHtml(result.getBody())
                );
    }
    @GetMapping("/refresh-test")
    public String refreshTest(
            HttpSession session) throws Exception {

        RestTemplate restTemplate =
                new RestTemplate();

        String refreshToken =
                (String) session.getAttribute(
                        "SMART_REFRESH_TOKEN"
                );

        if (refreshToken == null
                || refreshToken.isBlank()) {

            return "No refresh token in session. Run SMART authorization first.";
        }

        JsonNode discovery =
                objectMapper.readTree(
                        restTemplate.getForObject(
                                discoveryUrl,
                                String.class
                        )
                );

        String tokenEndpoint =
                discovery
                        .get("token_endpoint")
                        .asText();

        HttpHeaders headers =
                new HttpHeaders();

        headers.setContentType(
                MediaType.APPLICATION_FORM_URLENCODED
        );

        headers.setBasicAuth(
                clientId,
                clientSecret
        );

        MultiValueMap<String, String> body =
                new LinkedMultiValueMap<>();

        body.add(
                "grant_type",
                "refresh_token"
        );

        body.add(
                "refresh_token",
                refreshToken
        );

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(
                        body,
                        headers
                );

        ResponseEntity<String> result =
                restTemplate.postForEntity(
                        tokenEndpoint,
                        request,
                        String.class
                );

        JsonNode tokenJson =
                objectMapper.readTree(
                        result.getBody()
                );

        boolean newAccessToken =
                tokenJson.has("access_token")
                && !tokenJson.get("access_token").isNull()
                && !tokenJson.get("access_token").asText().isBlank();

        boolean newRefreshToken =
                tokenJson.has("refresh_token")
                && !tokenJson.get("refresh_token").isNull()
                && !tokenJson.get("refresh_token").asText().isBlank();

        if (newRefreshToken) {
            session.setAttribute(
                    "SMART_REFRESH_TOKEN",
                    tokenJson.get("refresh_token").asText()
            );
        }

        return """
                <html>
                <body>
                <h1>Refresh Token Test</h1>
                <p>Token HTTP Status: %s</p>
                <p>New Access Token Received: %s</p>
                <p>New Refresh Token Received: %s</p>
                </body>
                </html>
                """.formatted(
                        result.getStatusCode(),
                        newAccessToken,
                        newRefreshToken
                );
    }
    private boolean hasRefreshToken(
            String tokenResponse) {

        if (tokenResponse == null) {
            return false;
        }

        try {
            JsonNode tokenJson =
                    objectMapper.readTree(tokenResponse);

            JsonNode refreshTokenNode =
                    tokenJson.get("refresh_token");

            return refreshTokenNode != null
                    && !refreshTokenNode.isNull()
                    && !refreshTokenNode.asText().isBlank();

        } catch (Exception e) {
            return false;
        }
    }
    private String extractAccessToken(
            String tokenResponse)
            throws Exception {

        if (tokenResponse == null) {
            return null;
        }

        JsonNode tokenJson =
                objectMapper.readTree(
                        tokenResponse
                );

        JsonNode accessTokenNode =
                tokenJson.get(
                        "access_token"
                );

        if (accessTokenNode == null
                || accessTokenNode.isNull()) {

            return null;
        }

        return accessTokenNode.asText();
    }


    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}







