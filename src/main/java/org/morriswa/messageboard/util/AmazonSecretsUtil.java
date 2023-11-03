package org.morriswa.messageboard.util;

public interface AmazonSecretsUtil {
    /**
     * Retrieves a secret
     * @param key of secret to retrieve
     * @throws NullPointerException if the secret cannot be found
     * @return a secret
     */
    String retrieveKey(String key) throws NullPointerException;
}
