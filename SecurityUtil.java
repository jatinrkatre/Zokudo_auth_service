package com.zokudo.sor.util;


import com.google.common.collect.Maps;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import javax.validation.constraints.NotBlank;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class SecurityUtil {

    private final String springPassword;
    private final String applicationLevelUserName;
    private final String AUTHORIZATION_TOKEN_PREFIX = "Bearer ";
    private static final SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
    private static final int TOKEN_EXPIRY_DURATION_IN_MIN = 30;
    private static final String JWT_TOKEN_ISSUER = "ODINMO";
    private static final String AUTHORIZATION_SUBJECT = "Authorization Token";
    private final String secretKey;
    private final SecretKeySpec secretKeySpec;

    @Autowired
    public SecurityUtil(@Value("${applicationLevel.user.password}") final String springPassword,
                        @Value("${applicationLevel.user.name}") final String applicationLevelUserName,
                        @Value("${spring.security.user.password}") final String secretKey) {
        this.springPassword = springPassword;
        this.applicationLevelUserName = applicationLevelUserName;
        this.secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
        this.secretKeySpec=new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), signatureAlgorithm.getJcaName());
    }


    public String getBearerAuthorizationHeader(final String token) {
        return "Bearer " + token;
    }


    public String getAuthorizationHeader() {
        try {
            log.info(" Generating JWT Token. ");
            String jwtToken  = generateJwtToken(this.applicationLevelUserName, Maps.newHashMap());
            return jwtToken;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return "";
        }
    }

    public String generateJwtToken(@NotBlank(message = "username is mandatory") final String username, final Map<String, Object> payload) {
         return AUTHORIZATION_TOKEN_PREFIX.concat(Jwts.builder()
                 .setHeader(getJwtHeader(signatureAlgorithm.getValue()))
                 .setId(UUID.randomUUID().toString())
                 .setSubject(AUTHORIZATION_SUBJECT)
                 .setIssuedAt(new Date())
                 .setIssuer(JWT_TOKEN_ISSUER)
                 .setAudience(username)
                 .setExpiration(getJwtExpiryDate())
                 .addClaims(getPayload(payload))
                 .signWith(signatureAlgorithm,secretKey)
                 .compact());
    }


    private Map<String,Object> getJwtHeader(final String algrothim){
        final HashMap<String,Object> headers = Maps.newHashMap();
        headers.put("typ","JWT");
        headers.put("alg", algrothim);
        return headers;
    }

    private Date getJwtExpiryDate() {
        return LocalDateTime.now().plusMinutes(TOKEN_EXPIRY_DURATION_IN_MIN).toDate();
    }

    private Map<String, Object> getPayload(final Map<String, Object> payload) {
        if (Objects.isNull(payload)) {
            return Maps.newHashMap();
        }
        return payload;
    }
}
