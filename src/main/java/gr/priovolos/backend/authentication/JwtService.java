package gr.priovolos.backend.authentication;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Service responsible for generating, signing, parsing and validating
 * JSON Web Tokens (JWT) used by the application.
 *
 * <p>This service creates signed JWTs after successful authentication and
 * validates incoming tokens for protected REST API requests.</p>
 *
 * <p>JWTs are signed using the HS256 (HMAC-SHA256) algorithm and contain
 * the authenticated user's username as the subject together with the
 * user's role as a custom claim.</p>
 *
 * <p>The signing key and token expiration time are loaded from the
 * application's configuration properties.</p>
 *
 * @author Ioannis Priovolos
 */
@Service
public class JwtService {

    /**
     * Base64-encoded secret key used to sign and verify JWTs.
     */
    @Value("${app.security.secret-key}")
    private String secretKey;

    /**
     * JWT validity period in milliseconds.
     */
    @Value("${app.security.jwt-expiration}")
    private long jwtExpiration;

    /**
     * Generates a signed JWT for an authenticated user.
     *
     * <p>The generated token contains:
     * <ul>
     *     <li>The token issuer.</li>
     *     <li>The authenticated username as the JWT subject.</li>
     *     <li>The user's role as a custom claim.</li>
     *     <li>The token issue date.</li>
     *     <li>The token expiration date.</li>
     * </ul>
     *
     * @param username the authenticated user's username
     * @param role the authenticated user's role
     * @return a signed JWT
     */
    public String generateToken(String username, String role) {
        Date issuedAt = new Date();
        Date expiration = new Date(
                issuedAt.getTime() + jwtExpiration
        );

        return Jwts.builder()
                .issuer("https://api.codingfactory.gr")
                .subject(username)
                .claim("role", role)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(getSignInKey(), Jwts.SIG.HS256)
                .compact();
    }

    /**
     * Determines whether a JWT is valid for the specified user.
     *
     * <p>A token is considered valid if:
     * <ul>
     *     <li>its subject matches the authenticated user's username, and</li>
     *     <li>it has not expired.</li>
     * </ul>
     *
     * @param token the JWT to validate
     * @param userDetails the authenticated user's details
     * @return {@code true} if the token is valid; otherwise {@code false}
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String subject = extractSubject(token);
        return (subject.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Retrieves a custom String claim from a JWT.
     *
     * @param token the JWT
     * @param claim the name of the claim to retrieve
     * @return the claim value
     */
    public String getStringClaim(String token, String claim) {
        return extractAllClaims(token).get(claim, String.class);
    }

    /**
     * Extracts the username (JWT subject) from a token.
     *
     * @param token the JWT
     * @return the username stored as the JWT subject
     */
    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extracts a specific claim from a JWT using the supplied resolver.
     *
     * @param token the JWT
     * @param claimsResolver function used to resolve the desired claim
     * @param <T> the type of the returned claim
     * @return the extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Determines whether a JWT has expired.
     *
     * @param token the JWT
     * @return {@code true} if the token has expired; otherwise {@code false}
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extracts the expiration date from a JWT.
     *
     * @param token the JWT
     * @return the token expiration date
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parses and verifies a signed JWT.
     *
     * <p>The token signature is verified using the configured HMAC secret
     * key before the claims are returned.</p>
     *
     * @param token the JWT
     * @return the verified JWT claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .requireIssuer("https://api.codingfactory.gr")
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Builds the cryptographic signing key used for JWT generation
     * and signature verification.
     *
     * <p>The key is decoded from its Base64 representation and converted
     * into a {@link SecretKey} suitable for the HS256 algorithm.</p>
     *
     * @return the JWT signing key
     */
    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}