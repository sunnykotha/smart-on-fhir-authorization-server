package smart_on_fhir_server;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthDebugController {

    @GetMapping("/debug/auth")
    public String auth() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "AUTHENTICATION = null";
        }

        return "AUTHENTICATION = "
                + authentication.getClass().getName()
                + "\nNAME = "
                + authentication.getName()
                + "\nAUTHENTICATED = "
                + authentication.isAuthenticated()
                + "\nPRINCIPAL = "
                + authentication.getPrincipal();
    }
}
