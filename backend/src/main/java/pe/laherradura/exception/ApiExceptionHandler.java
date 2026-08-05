package pe.laherradura.exception;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.OffsetDateTime;
import java.util.*;
@RestControllerAdvice
public class ApiExceptionHandler {
 @ExceptionHandler(NotFoundException.class) ResponseEntity<?> notFound(NotFoundException e){return body(HttpStatus.NOT_FOUND,e.getMessage(),null);}
 @ExceptionHandler(BusinessException.class) ResponseEntity<?> business(BusinessException e){return body(HttpStatus.BAD_REQUEST,e.getMessage(),null);}
 @ExceptionHandler(MethodArgumentNotValidException.class) ResponseEntity<?> validation(MethodArgumentNotValidException e){
   Map<String,String> errors=new LinkedHashMap<>();e.getBindingResult().getFieldErrors().forEach(x->errors.put(x.getField(),x.getDefaultMessage()));
   return body(HttpStatus.BAD_REQUEST,"Datos inválidos",errors);
 }
 @ExceptionHandler(Exception.class) ResponseEntity<?> general(Exception e){return body(HttpStatus.INTERNAL_SERVER_ERROR,"Error interno",Map.of("detail",e.getMessage()==null?"":e.getMessage()));}
 private ResponseEntity<?> body(HttpStatus s,String m,Object d){return ResponseEntity.status(s).body(Map.of("timestamp",OffsetDateTime.now().toString(),"status",s.value(),"message",m,"details",d==null?Map.of():d));}
}
