package br.com.autonomiccs.wakeonlan.initialization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import br.com.autonomiccs.autonomic.plugin.common.utils.ShellCommandUtils;

/**
 * This class initializes and configures the application
 */

@Configuration
@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan("br.com.autonomiccs.wakeonlan")
public class InicializeSystem {

    public static void main(String[] args) {
        SpringApplication.run(InicializeSystem.class, args);
    }

    /**
     * It creates a {@link ShellCommandUtils} object
     *
     * @return {@link ShellCommandUtils}
     */
    @Bean
    public ShellCommandUtils createShellCommandUtils() {
        return new ShellCommandUtils();
    }
}