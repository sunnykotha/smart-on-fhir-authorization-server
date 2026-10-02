package smart_on_fhir_server.encounter.controller;

import java.util.List;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import smart_on_fhir_server.encounter.Encounter;
import smart_on_fhir_server.encounter.service.EncounterService;

@RestController
@RequestMapping("/fhir")
public class EncounterController {

    private final EncounterService encounterService;

    public EncounterController(
            EncounterService encounterService) {

        this.encounterService = encounterService;
    }

    @GetMapping("/Encounter")
    public List<Encounter> getEncounters(
            @RequestParam(required = false) String id,
            JwtAuthenticationToken authentication) {

        return encounterService.findAuthorizedEncounter(
                authentication,
                id
        );
    }
}
