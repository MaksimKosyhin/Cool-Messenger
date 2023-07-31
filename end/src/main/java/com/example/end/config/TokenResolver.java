package com.example.end.config;

import com.example.end.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TokenResolver {
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;

    public String getToken(String subject) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24 * 7;

        var claims =
                JwtClaimsSet.builder()
                        .issuer("example.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(subject)
                        .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getToken(String subject, Map<String, Object> claims) {
        var now = Instant.now();
        var expiry = 60 * 60 * 24;

        var claimsSet =
                JwtClaimsSet.builder()
                        .issuer("example.com")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(expiry))
                        .subject(subject)
                        .claims(all -> all.putAll(claims))
                        .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claimsSet)).getTokenValue();
    }

    public Jwt decodeFromToken(String token) {
        Jwt jwt;

        try{
            jwt = jwtDecoder.decode(token);
        } catch (JwtException ex) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "invalid token");
        }

        if(jwt.getExpiresAt().isBefore(Instant.now())) {
            var msg = "Confirmation token has expired. Try to send another confirmation";
            throw new ApiException(HttpStatus.UNAUTHORIZED, msg);
        }

        return jwt;
    }

}
