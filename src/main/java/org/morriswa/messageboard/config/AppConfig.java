package org.morriswa.messageboard.config;

import com.amazonaws.services.appconfigdata.AWSAppConfigDataClient;
import com.amazonaws.services.appconfigdata.model.GetLatestConfigurationRequest;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionRequest;
import com.amazonaws.services.appconfigdata.model.StartConfigurationSessionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.vault.support.JsonMapFlattener;

import java.io.UnsupportedEncodingException;
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

    private final AWSAppConfigDataClient client;
    private final StartConfigurationSessionResult session;

    public AppConfig() {
        // create new session request with captured environment variables
        var sessionConfig = new StartConfigurationSessionRequest();
        sessionConfig.setApplicationIdentifier(System.getenv("APPCONFIG_APPLICATION_ID"));
        sessionConfig.setConfigurationProfileIdentifier(System.getenv("APPCONFIG_PROFILE_ID"));
        sessionConfig.setEnvironmentIdentifier(System.getenv("APPCONFIG_ENV_ID"));
        // create a client and start a session to be used to retrieve config
        client = (AWSAppConfigDataClient) AWSAppConfigDataClient.builder().withRegion("us-east-2").build();
        session = client.startConfigurationSession(sessionConfig);
    }

    /**
     * Retrieves latest config from AWS as String
     *
     * @return String containing config file
     * @throws UnsupportedEncodingException when aws returns config in a format not expected
     */
    private String retrieveLatestConfiguration() throws UnsupportedEncodingException {
        // get latest config profile from aws
        var configRequest = new GetLatestConfigurationRequest();
        configRequest.setConfigurationToken(session.getInitialConfigurationToken());
        var result = client.getLatestConfiguration(configRequest);
        // parse as string and return
        return StandardCharsets.UTF_8.decode(result.getConfiguration()).toString();
    }

    /**
     * Get the latest Property Source for currently configured application
     *
     * @return a Property Source to be imported to spring
     * @throws UnsupportedEncodingException when aws returns config in a format not expected
     * @throws JsonProcessingException if YAML file cannot be read
     */
    public PropertiesPropertySource retrieveApplicationPropertySource(String application) throws UnsupportedEncodingException, JsonProcessingException {
        // create new object mapper and type refrence
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {};
        // create a deep config map from YAML file using mapper and type ref
        Map<String, Object> configMap = mapper.readValue(retrieveLatestConfiguration(), typeRef);
        // flatten deep config and add to AWS_PROPS source

        var config = JsonMapFlattener.flatten(configMap);

        config.put("server.port",config.get(String.format("%s.service.port",application)));

        log.info("Got port {} from config", config.get("server.port"));

        return new PropertiesPropertySource("AWS_PROPS", new Properties() {{
            this.putAll(config);
        }});
    }
}
