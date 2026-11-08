package com.sunfeax.citeria.security;

import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import com.sunfeax.citeria.exception.FeatureUnavailableException;
import com.sunfeax.citeria.exception.UnauthorizedException;

@Component
public class GoogleIdTokenVerifier {

    private static final String JWK_SET_URI = "https://www.googleapis.com/oauth2/v3/certs";
    private static final Set<String> ISSUERS = Set.of("accounts.google.com", "https://accounts.google.com");

    private final String clientId;
    private final NimbusJwtDecoder decoder;

    public GoogleIdTokenVerifier(@Value("${app.google.client-id:}") String clientId) {
        this.clientId = clientId;
        this.decoder = NimbusJwtDecoder.withJwkSetUri(JWK_SET_URI).build();
        this.decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new JwtTimestampValidator(),
            this::validateIssuerAndAudience
        ));
    }

    public boolean isConfigured() {
        return !clientId.isBlank();
    }

    public GoogleProfile verify(String credential) {
        if (!isConfigured()) {
            throw new FeatureUnavailableException("Google sign-in is not configured.");
        }

        try {
            Jwt jwt = decoder.decode(credential);
            return new GoogleProfile(
                jwt.getSubject(),
                jwt.getClaimAsString("email"),
                Boolean.TRUE.equals(jwt.getClaimAsBoolean("email_verified")),
                jwt.getClaimAsString("given_name"),
                jwt.getClaimAsString("family_name")
            );
        } catch (JwtException ex) {
            throw new UnauthorizedException("Google credential is invalid.");
        }
    }

    private OAuth2TokenValidatorResult validateIssuerAndAudience(Jwt jwt) {
        boolean issuerMatches = ISSUERS.contains(jwt.getClaimAsString(JwtClaimNames.ISS));
        boolean audienceMatches = jwt.getAudience() != null && jwt.getAudience().contains(clientId);

        if (issuerMatches && audienceMatches) {
            return OAuth2TokenValidatorResult.success();
        }

        return OAuth2TokenValidatorResult.failure(
            new OAuth2Error("invalid_token", "Token was not issued by Google for this application.", null)
        );
    }
}
