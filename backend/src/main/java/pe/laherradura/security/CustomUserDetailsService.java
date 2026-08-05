package pe.laherradura.security;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import pe.laherradura.entity.AppUser;
import pe.laherradura.repository.AppUserRepository;
@Service
public class CustomUserDetailsService implements UserDetailsService {
 private final AppUserRepository repo;
 public CustomUserDetailsService(AppUserRepository repo){this.repo=repo;}
 @Override public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
   AppUser u=repo.findByUsername(username).orElseThrow(()->new UsernameNotFoundException("Usuario no encontrado"));
   return User.withUsername(u.getUsername()).password(u.getPassword()).roles(u.getRole().name())
       .disabled(!u.isActive()).build();
 }
}
