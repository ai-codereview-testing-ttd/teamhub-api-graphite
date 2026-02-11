package com.teamhub.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.teamhub.config.AppConfig;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

class JwtHelperTest {

    @Test
    void generateToken_returnsValidToken() {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void validateToken_validToken() {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        JWTClaimsSet claims = JwtHelper.validateToken(token);

        assertNotNull(claims);
        assertEquals("user-1", claims.getSubject());
    }

    @Test
    void validateToken_extractsClaims() throws ParseException {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        JWTClaimsSet claims = JwtHelper.validateToken(token);

        assertNotNull(claims);
        assertEquals("user-1", claims.getSubject());
        assertEquals("user@test.com", claims.getStringClaim("email"));
        assertEquals("org-1", claims.getStringClaim("organizationId"));
        assertEquals(AppConfig.JWT_ISSUER, claims.getIssuer());
    }

    @Test
    void validateToken_expiredToken() throws JOSEException {
        // Manually create an expired token
        MACSigner signer = new MACSigner(AppConfig.JWT_SECRET.getBytes());
        Date past = new Date(System.currentTimeMillis() - 100000);

        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("user-1")
                .issuer(AppConfig.JWT_ISSUER)
                .expirationTime(past)
                .build();

        SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
        signedJWT.sign(signer);

        JWTClaimsSet result = JwtHelper.validateToken(signedJWT.serialize());
        assertNull(result);
    }

    @Test
    void validateToken_invalidToken() {
        JWTClaimsSet claims = JwtHelper.validateToken("not-a-valid-token");
        assertNull(claims);
    }

    @Test
    void validateToken_tamperedToken() {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        // Tamper with the token by modifying a character
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        JWTClaimsSet claims = JwtHelper.validateToken(tampered);
        assertNull(claims);
    }

    @Test
    void extractClaim_existingClaim() {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        String email = JwtHelper.extractClaim(token, "email");
        assertEquals("user@test.com", email);
    }

    @Test
    void extractClaim_nonExistentClaim() {
        String token = JwtHelper.generateToken("user-1", "user@test.com", "org-1");
        String value = JwtHelper.extractClaim(token, "nonexistent");
        assertNull(value);
    }

    @Test
    void extractClaim_invalidToken() {
        String value = JwtHelper.extractClaim("invalid-token", "email");
        assertNull(value);
    }
}
