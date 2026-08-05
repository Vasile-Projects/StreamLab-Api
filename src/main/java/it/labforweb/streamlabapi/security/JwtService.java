package it.labforweb.streamlabapi.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import it.labforweb.streamlabapi.models.Utente;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {

    private final Key key;
    private final JwtProperties jwtProperties;
    public static final String CLAIM_USER_ID = "userId";

    public JwtService (JwtProperties jwtProperties) {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtProperties.getSecret()));
        this.jwtProperties = jwtProperties;
    }

    public String generateAccessToken(Utente utente){

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getAccessTokenExpirationMs());
        return Jwts.builder()
                .subject(utente.getEmail())
                .claim(CLAIM_USER_ID, utente.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Utente utente){

        Instant now = Instant.now();
        Instant expiration = now.plusMillis(jwtProperties.getRefreshTokenExpirationMs());
        return Jwts.builder()
                .subject(utente.getEmail())
                .claim(CLAIM_USER_ID, utente.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseAndValidate(String token){
        return Jwts.parser()
                .verifyWith((SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

}
