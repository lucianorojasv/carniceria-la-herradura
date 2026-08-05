package pe.laherradura.controller;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.laherradura.service.WhatsAppService;
@RestController @RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {
 private final WhatsAppService s;private final ObjectMapper mapper;
 public WhatsAppWebhookController(WhatsAppService s,ObjectMapper mapper){this.s=s;this.mapper=mapper;}
 @GetMapping public ResponseEntity<String> verify(@RequestParam(name="hub.mode",required=false) String mode,@RequestParam(name="hub.verify_token",required=false) String token,@RequestParam(name="hub.challenge",required=false) String challenge){try{return ResponseEntity.ok(s.verify(mode,token,challenge));}catch(Exception e){return ResponseEntity.status(403).body("Forbidden");}}
 @PostMapping public ResponseEntity<String> receive(@RequestBody String body,@RequestHeader(name="X-Hub-Signature-256",required=false) String signature){try{if(!s.validSignature(body,signature))return ResponseEntity.status(401).body("Invalid signature");s.receive(mapper.readTree(body));return ResponseEntity.ok("EVENT_RECEIVED");}catch(Exception e){return ResponseEntity.badRequest().body("Invalid payload");}}
}
