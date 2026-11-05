package in.wb.nhls.user.service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import in.wb.nhls.config.JwtConfig;
import in.wb.nhls.user.entity.AppUser;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final Duration expiry;

    public TokenService(JwtEncoder jwtEncoder,
            @Value("${nhls.jwt.expiry-minutes:60}") long expiryMinutes) {
        this.jwtEncoder = jwtEncoder;
        this.expiry = Duration.ofMinutes(expiryMinutes);
    }

    public String issue(AppUser user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(JwtConfig.ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(expiry))
                .subject(user.getUsername())
                .claim(JwtConfig.ROLES_CLAIM, List.of(user.getRole().name()))
                .claim("name", user.getFullName())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    public long expirySeconds() {
        return expiry.toSeconds();
    }
}
