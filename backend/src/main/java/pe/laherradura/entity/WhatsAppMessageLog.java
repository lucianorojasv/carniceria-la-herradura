package pe.laherradura.entity;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
@Entity
@Table(name="whatsapp_message_log")
public class WhatsAppMessageLog {
 @Id @Column(length=120) private String messageId;
 @Column(length=30) private String phone;
 @Column(nullable=false) private OffsetDateTime processedAt=OffsetDateTime.now();
 public String getMessageId(){return messageId;} public void setMessageId(String v){messageId=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public OffsetDateTime getProcessedAt(){return processedAt;} public void setProcessedAt(OffsetDateTime v){processedAt=v;}
}
