package br.com.autonomiccs.autonomic.plugin.common.daos.configurations;

import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.cloud.utils.db.DbProperties;

/**
 * Configure data base properties.
 */
@Configuration
public class ConfigureDataBaseProperties {

    @Bean(name = "db.properties")
    public Properties getPropertiesFileFromApacheCloudStack() {
        return DbProperties.getDbProperties();
    }
}
