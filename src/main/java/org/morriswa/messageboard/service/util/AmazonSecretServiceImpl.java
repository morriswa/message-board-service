package org.morriswa.messageboard.service.util;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.morriswa.messageboard.service.util.AmazonSecretService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
@Service
@Profile("!test")
public class AmazonSecretServiceImpl implements AmazonSecretService {
    private final Map<String,Object> secrets;

    @Autowired
    public AmazonSecretServiceImpl(Environment env) throws JsonProcessingException {
        this.secrets = getSecret(
                env.getProperty("aws.secret-name"),
                env.getProperty("aws.region"));
    }

    private static Map<String, Object> getSecret(String secretName, String region) throws JsonProcessingException {
        // Create a Secrets Manager client
        AWSSecretsManager client  = AWSSecretsManagerClientBuilder
                .standard()
                .withRegion(region)
                .build();

        // In this sample we only handle the specific exceptions for the 'GetSecretValue' API.
        // See https://docs.aws.amazon.com/secretsmanager/latest/apireference/API_GetSecretValue.html
        // We rethrow the exception by default. I removed handling here

        String secret, decodedBinarySecret;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest()
                .withSecretId(secretName);
        GetSecretValueResult getSecretValueResult = client.getSecretValue(getSecretValueRequest);

        // Decrypts secret using the associated KMS key.
        // Depending on whether the secret is a string or binary, one of these fields will be populated.
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            return new ObjectMapper().readValue(secret, HashMap.class);
        } else {
            decodedBinarySecret = new String(Base64.getDecoder().decode(getSecretValueResult.getSecretBinary()).array());
            return new ObjectMapper().readValue(decodedBinarySecret, HashMap.class);
        }
    }

    @Override
    public String retrieveKey(String key) {
        if (this.secrets.containsKey(key)) {
            return (String) this.secrets.get(key);
        }
        throw new NullPointerException(String.format("No secret found with key:%s",key));
    }
}

