package pe.laherradura.controller;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.dto.*;
import pe.laherradura.service.ChatbotService;
@RestController @RequestMapping("/api/chat")
public class ChatController {private final ChatbotService s;public ChatController(ChatbotService s){this.s=s;}@PostMapping("/message") public ChatMessageResponse message(@Valid @RequestBody ChatMessageRequest r){return s.process(r.phone(),r.customerName(),r.message());}}
