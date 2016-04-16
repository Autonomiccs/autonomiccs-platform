package br.com.autonomiccs.wakeonlan.jersey;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

/**
 * This class provides the package(s) in which the rest resources are.
 */
@Component
@ApplicationPath("/boot")
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        this.packages("br.com.autonomiccs.wakeonlan.controller");
    }

}
