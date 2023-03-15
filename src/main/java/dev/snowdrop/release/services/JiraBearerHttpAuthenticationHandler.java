package dev.snowdrop.release.services;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;

public class JiraBearerHttpAuthenticationHandler implements AuthenticationHandler {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final String token;

    public JiraBearerHttpAuthenticationHandler(final String token) {
        this.token = token;
    }


    @Override
    public void configure(Request.Builder builder) {
        builder.setHeader(AUTHORIZATION_HEADER, "Bearer " + token);
    }
}
