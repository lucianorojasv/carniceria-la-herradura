package pe.laherradura.security;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
@Service
public class JwtService {
 private final SecretKey key;
 private final long expirationMinutes;
 public JwtService(@Value("${app.jwt.secret}") String secret,
                   @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
   byte[] bytes = secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() % 4 == 0
       ? safeDecode(secret) : secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
   if (bytes.length < 32) throw new IllegalArgumentException("JWT_SECRET debe tener al menos 32 bytes");
   this.key = Keys.hmacShaKeyFor(bytes);
   this.expirationMinutes = expirationMinutes;
 }
 private byte[] safeDecode(String secret){
   try{return Decoders.BASE64.decode(secret);}catch(Exception e){return secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);}
 }
 public String generate(UserDetails user, String role, String fullName) {
   Instant now=Instant.now();
   return Jwts.builder().subject(user.getUsername()).claim("role",role).claim("name",fullName)
       .issuedAt(Date.from(now)).expiration(Date.from(now.plusSeconds(expirationMinutes*60)))
       .signWith(key).compact();
 }
 public String username(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getSubject();}
 public boolean valid(String token, UserDetails user){
   try{return username(token).equals(user.getUsername()) &&
       Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload().getExpiration().after(new Date());}
   catch(Exception e){return false;}
 }
}
