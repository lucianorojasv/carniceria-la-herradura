package pe.laherradura.service;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import pe.laherradura.repository.ProductRepository;
import java.util.*;
@Service
public class OpenAiRecommendationService {
 private final boolean enabled;private final String key;private final String model;private final ProductRepository products;private final RestClient rest;
 public OpenAiRecommendationService(@Value("${app.openai.enabled}") boolean enabled,@Value("${app.openai.api-key}") String key,@Value("${app.openai.model}") String model,ProductRepository products,RestClient.Builder b){this.enabled=enabled;this.key=key;this.model=model;this.products=products;this.rest=b.baseUrl("https://api.openai.com").build();}
 public String recommend(String question){
   String catalog=products.findByActiveTrueOrderByNameAsc().stream().map(p->p.getName()+" (S/ "+p.getPricePerUnit()+" por "+p.getUnit()+")").reduce((a,b)->a+", "+b).orElse("catálogo pendiente");
   if(!enabled||key==null||key.isBlank())return fallback(question,catalog);
   try{
     Map<String,Object> body=new LinkedHashMap<>();body.put("model",model);body.put("store",false);
     body.put("input","Eres Mashico, asistente de Carnicería La Herradura. Responde en español peruano, breve y vendedor. Recomienda solo productos del catálogo y no inventes precios. Catálogo: "+catalog+". Consulta: "+question);
     JsonNode n=rest.post().uri("/v1/responses").header("Authorization","Bearer "+key).contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JsonNode.class);
     if(n!=null&&n.has("output"))for(JsonNode out:n.get("output"))if(out.has("content"))for(JsonNode c:out.get("content"))if(c.has("text"))return c.get("text").asText();
   }catch(Exception ignored){}
   return fallback(question,catalog);
 }
 private String fallback(String q,String catalog){
   String s=q.toLowerCase(Locale.ROOT);
   if(s.contains("parrilla"))return "Para parrilla te recomiendo picaña, ribeye, asado de tira o entraña. Escribe *2* para armar tu pedido. Productos disponibles: "+catalog;
   if(s.contains("guis")||s.contains("estof"))return "Para guiso convienen cortes con buen sabor y cocción lenta. Revisa el catálogo disponible y escribe *2* para pedir: "+catalog;
   return "Puedo ayudarte a elegir el corte ideal. Indícame qué plato prepararás y para cuántas personas. También puedes escribir *menu*.";
 }
}
