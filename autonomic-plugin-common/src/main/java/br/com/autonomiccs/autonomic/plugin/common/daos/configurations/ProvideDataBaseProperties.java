package br.com.autonomiccs.autonomic.plugin.common.daos.configurations;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloud.utils.db.DbProperties;

/**
 * Provide database.properties files to be accessible to the Autonomiccs plugins application context
 */
@Configuration
public class ProvideDataBaseProperties {

    @Bean(name = "db.properties")
    public Properties getPropertiesFileFromApacheCloudStack() {
        return DbProperties.getDbProperties();
    }
}
