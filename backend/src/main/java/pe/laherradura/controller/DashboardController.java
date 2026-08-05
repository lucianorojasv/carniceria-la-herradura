package pe.laherradura.controller;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.DashboardResponse;
import pe.laherradura.service.DashboardService;
@RestController @RequestMapping("/api/dashboard")
public class DashboardController {private final DashboardService s;public DashboardController(DashboardService s){this.s=s;}@GetMapping public DashboardResponse get(){return s.get();}}
