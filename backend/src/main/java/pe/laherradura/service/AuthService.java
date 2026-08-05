package pe.laherradura.service;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import pe.laherradura.dto.*;
import pe.laherradura.entity.AppUser;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.AppUserRepository;
import pe.laherradura.security.*;
@Service
public class AuthService {
 private final AuthenticationManager manager; private final CustomUserDetailsService users; private final JwtService jwt; private final AppUserRepository repo;
 public AuthService(AuthenticationManager m,CustomUserDetailsService u,JwtService j,AppUserRepository r){manager=m;users=u;jwt=j;repo=r;}
 public AuthResponse login(AuthRequest req){
   manager.authenticate(new UsernamePasswordAuthenticationToken(req.username(),req.password()));
   UserDetails ud=users.loadUserByUsername(req.username());
   AppUser u=repo.findByUsername(req.username()).orElseThrow(()->new NotFoundException("Usuario no encontrado"));
   return new AuthResponse(jwt.generate(ud,u.getRole().name(),u.getFullName()),u.getUsername(),u.getFullName(),u.getRole().name());
 }
}
