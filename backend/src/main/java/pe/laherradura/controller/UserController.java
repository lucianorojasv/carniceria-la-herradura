package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.ChangePasswordRequest;
import pe.laherradura.service.UserService;
import java.util.Map;
@RestController @RequestMapping("/api/users")
public class UserController {private final UserService s;public UserController(UserService s){this.s=s;}@PostMapping("/me/password") public ResponseEntity<?> password(@Valid @RequestBody ChangePasswordRequest r){s.changePassword(r);return ResponseEntity.ok(Map.of("message","Contraseña actualizada"));}}
