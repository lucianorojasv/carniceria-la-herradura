package pe.laherradura.service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.ChangePasswordRequest;
import pe.laherradura.entity.AppUser;
import pe.laherradura.exception.BusinessException;
import pe.laherradura.repository.AppUserRepository;
@Service @Transactional
public class UserService {
 private final AppUserRepository repo;private final PasswordEncoder encoder;
 public UserService(AppUserRepository r,PasswordEncoder e){repo=r;encoder=e;}
 public void changePassword(ChangePasswordRequest req){String username=SecurityContextHolder.getContext().getAuthentication().getName();AppUser u=repo.findByUsername(username).orElseThrow(()->new BusinessException("Usuario no encontrado"));if(!encoder.matches(req.currentPassword(),u.getPassword()))throw new BusinessException("La contraseña actual no es correcta");if(req.currentPassword().equals(req.newPassword()))throw new BusinessException("La nueva contraseña debe ser diferente");u.setPassword(encoder.encode(req.newPassword()));repo.save(u);}
}
