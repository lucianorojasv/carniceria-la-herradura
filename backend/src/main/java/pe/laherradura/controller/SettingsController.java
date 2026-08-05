package pe.laherradura.controller;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.service.SettingsService;
@RestController @RequestMapping("/api/settings")
public class SettingsController {private final SettingsService s;public SettingsController(SettingsService s){this.s=s;}@GetMapping public BusinessSetting get(){return s.get();}@PutMapping public BusinessSetting save(@RequestBody BusinessSetting b){return s.save(b);}}
