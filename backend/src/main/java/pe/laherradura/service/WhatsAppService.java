package pe.laherradura.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.laherradura.dto.ChatMessageResponse;
import pe.laherradura.entity.WhatsAppMessageLog;
import pe.laherradura.repository.WhatsAppMessageLogRepository;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

@Service
public class WhatsAppService {
    private static final Logger log=LoggerFactory.getLogger(WhatsAppService.class);
    private final boolean enabled; private final String version; private final String phoneNumberId; private final String token;
    private final String appSecret; private final String verifyToken; private final ChatbotService chatbot; private final RestClient rest;
    private final WhatsAppMessageLogRepository logs;
    public WhatsAppService(@Value("${app.whatsapp.enabled}") boolean enabled,
                           @Value("${app.whatsapp.graph-api-version}") String version,
                           @Value("${app.whatsapp.phone-number-id}") String phoneNumberId,
                           @Value("${app.whatsapp.access-token}") String token,
                           @Value("${app.whatsapp.app-secret}") String appSecret,
                           @Value("${app.whatsapp.verify-token}") String verifyToken,
                           ChatbotService chatbot, RestClient.Builder builder, WhatsAppMessageLogRepository logs) {
        this.enabled=enabled;this.version=version;this.phoneNumberId=phoneNumberId;this.token=token;this.appSecret=appSecret;
        this.verifyToken=verifyToken;this.chatbot=chatbot;this.logs=logs;this.rest=builder.baseUrl("https://graph.facebook.com").build();
    }
    public String verify(String mode,String token,String challenge){if("subscribe".equals(mode)&&verifyToken.equals(token))return challenge;throw new IllegalArgumentException("Token de verificación inválido");}
    public boolean validSignature(String rawBody,String signature){
        if(appSecret==null||appSecret.isBlank())return true;
        if(signature==null||!signature.startsWith("sha256="))return false;
        try{Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(appSecret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));String calculated="sha256="+HexFormat.of().formatHex(mac.doFinal(rawBody.getBytes(StandardCharsets.UTF_8)));return MessageDigest.isEqual(calculated.getBytes(StandardCharsets.UTF_8),signature.getBytes(StandardCharsets.UTF_8));}catch(Exception e){return false;}
    }
    public void receive(JsonNode payload){
        JsonNode entries=payload.path("entry"); if(!entries.isArray())return;
        for(JsonNode entry:entries)for(JsonNode change:entry.path("changes")){
            JsonNode value=change.path("value"); String name=value.path("contacts").path(0).path("profile").path("name").asText("Cliente WhatsApp");
            for(JsonNode message:value.path("messages")){
                String messageId=message.path("id").asText();String from=message.path("from").asText();String type=message.path("type").asText();
                if(from.isBlank()||(!messageId.isBlank()&&logs.existsById(messageId)))continue;
                String text="text".equals(type)?message.path("text").path("body").asText():"menu";
                ChatMessageResponse response=chatbot.process(from,name,text);sendText(from,response.reply());
                if(!messageId.isBlank()){WhatsAppMessageLog l=new WhatsAppMessageLog();l.setMessageId(messageId);l.setPhone(from);logs.save(l);}
            }
        }
    }
    public void sendText(String to,String text){
        if(!enabled||phoneNumberId.isBlank()||token.isBlank()){log.info("WhatsApp desactivado. Respuesta para {}: {}",to,text.replace('\n',' '));return;}
        try{
            Map<String,Object> body=Map.of("messaging_product","whatsapp","recipient_type","individual","to",to,"type","text","text",Map.of("preview_url",false,"body",text));
            rest.post().uri("/"+version+"/"+phoneNumberId+"/messages").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().toBodilessEntity();
        }catch(Exception e){log.error("No se pudo enviar mensaje de WhatsApp a {}: {}",to,e.getMessage());}
    }
}
