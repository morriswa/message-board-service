package org.morriswa.messageboard.config;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.appconfigdata.AWSAppConfigDataClient;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationResult;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.vault.support.JsonMapFlattener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Class that once instantiated can retrieve Properties from AWS AppConfig for use in every application
 * <p>
 * MUST have the following environment variables set:
 *  APPCONFIG_APPLICATION_ID
 *  APPCONFIG_PROFILE_ID
 *  APPCONFIG_ENV_ID
 */
@Slf4j
public class AppConfig {
    /**
     * Get the latest Property Source for currently configured application
     *
     * @return a Property Source to be imported to spring
     * @throws JsonProcessingException if YAML file cannot be read
     */
    public static PropertiesPropertySource build() {
        // retrieve latest config from AWS
        final GetLatestConfigurationResult configurationResponse;
        {
            // fill in required information to retrieve config session
            var sessionConfig = new StartConfigurationSessionRequest();
            sessionConfig.setApplicationIdentifier(System.getenv("APPCONFIG_APPLICATION_ID"));
            sessionConfig.setConfigurationProfileIdentifier(System.getenv("APPCONFIG_PROFILE_ID"));
            sessionConfig.setEnvironmentIdentifier(System.getenv("APPCONFIG_ENV_ID"));

            // create a client and start a session to retrieve config
            var client = (AWSAppConfigDataClient) AWSAppConfigDataClient.builder().withRegion(Regions.US_EAST_2).build();
            var session = client.startConfigurationSession(sessionConfig);
            log.info("SUCCESSFULLY STARTED CONFIG SESSION, ENV {}",sessionConfig.getEnvironmentIdentifier());

            // fill in required info to retrieve latest config from AWS
            var configRequest = new GetLatestConfigurationRequest();
            configRequest.setConfigurationToken(session.getInitialConfigurationToken());

            // get latest config profile from aws
            configurationResponse = client.getLatestConfiguration(configRequest);
            log.info("SUCCESSFULLY RETRIEVED CONFIGURATION FILE");

            // shutdown client
            client.shutdown();
            log.info("SUCCESSFULLY SHUTDOWN CONFIG SESSION");
        }

        // map Config Result to Flat Properties Map
        final Map<String, Object> config;
        {
            // create new object mapper and type reference
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};

            // read configuration stream as UTF-8 String
            var rawConfig = StandardCharsets.UTF_8.decode(configurationResponse.getConfiguration()).toString();

            // create a deep config map from YAML String
            Map<String, Object> configMap;
            try {
                configMap = mapper.readValue(rawConfig, typeRef);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Encountered exception while reading properties file, cannot start application...", e);
            }

            // flatten and return properties map
            config = JsonMapFlattener.flatten(configMap);
        }

        log.info("SUCCESSFULLY BUILT SPRING PROPERTY SOURCE AWS_PROPS");
        // add all retrieved config keys to new property source and return
        return new PropertiesPropertySource("AWS_PROPS", new Properties() {{
            this.putAll(config);
        }});
    }
}
