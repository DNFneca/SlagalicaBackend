package org.civlabs.civlabsschedulebackend.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.webtoken.JsonWebSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthService {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${google.redirect-uri}")
    private String redirectUri;

    public GoogleIdToken.Payload verifyGoogleToken(String idTokenString)
            throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);

        if (idToken != null) {
            return idToken.getPayload();
        } else {
            throw new IllegalArgumentException("Invalid ID token.");
        }
    }

    public GoogleIdToken.Payload verifyByCode(String authCode) throws IOException, GeneralSecurityException {
        // 1. Exchange the "4/0A..." code for a real ID Token

        GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                new GsonFactory(),
                "https://oauth2.googleapis.com/token",
                clientId,
                clientSecret, // You need the Secret from Google Console here
                authCode,
                "postmessage")// Or your redirect URI
                .execute();

        // 2. Get the ID Token from the response
        String idTokenString = tokenResponse.getIdToken();

        // 3. Now verify the ID Token as we did before
        return verifyGoogleToken(idTokenString);
    }
}
