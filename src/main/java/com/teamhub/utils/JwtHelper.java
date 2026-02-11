package com.teamhub.utils;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.teamhub.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public final class JwtHelper {

    private static final Logger logger = LoggerFactory.getLogger(JwtHelper.class);

    private JwtHelper() {
        // Utility class
    }

    /**
     * Generate a JWT token for a given user.
     */
    public static String generateToken(String userId, String email, String organizationId) {
        try {
            JWSSigner signer = new MACSigner(AppConfig.JWT_SECRET.getBytes());

            long nowMillis = System.currentTimeMillis();
            Date now = new Date(nowMillis);
            Date expiry = new Date(nowMillis + (long) AppConfig.JWT_EXPIRY_SECONDS * 1000);

            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userId)
                    .issuer(AppConfig.JWT_ISSUER)
                    .claim("email", email)
                    .claim("organizationId", organizationId)
                    .issueTime(now)
                    .expirationTime(expiry)
                    .build();

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (JOSEException e) {
            logger.error("Failed to generate JWT token", e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate a JWT token and return the claims if valid.
     */
    public static JWTClaimsSet validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(AppConfig.JWT_SECRET.getBytes());

            if (!signedJWT.verify(verifier)) {
                logger.debug("JWT signature verification failed");
                return null;
            }

            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            // Check expiration
            Date expiration = claims.getExpirationTime();
            if (expiration != null && expiration.before(new Date())) {
                logger.debug("JWT token has expired");
                return null;
            }

            // Check issuer
            if (!AppConfig.JWT_ISSUER.equals(claims.getIssuer())) {
                logger.debug("JWT issuer mismatch");
                return null;
            }

            return claims;
        } catch (ParseException | JOSEException e) {
            logger.debug("Failed to parse or verify JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract a specific claim from a token.
     */
    public static String extractClaim(String token, String claimName) {
        JWTClaimsSet claims = validateToken(token);
        if (claims == null) {
            return null;
        }
        try {
            return claims.getStringClaim(claimName);
        } catch (ParseException e) {
            logger.debug("Failed to extract claim '{}': {}", claimName, e.getMessage());
            return null;
        }
    }
}
