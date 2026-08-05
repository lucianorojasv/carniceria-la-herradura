package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.*;
import pe.laherradura.service.AuthService;
@RestController @RequestMapping("/api/auth")
public class AuthController {private final AuthService service;public AuthController(AuthService s){service=s;}@PostMapping("/login") public AuthResponse login(@Valid @RequestBody AuthRequest r){return service.login(r);}}
