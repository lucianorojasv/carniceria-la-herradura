package pe.laherradura.entity;
import jakarta.persistence.*;
import pe.laherradura.enums.ChatState;
import java.time.OffsetDateTime;
@Entity
@Table(name="chat_sessions")
public class ChatSession {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=30) private String phone;
 @ManyToOne @JoinColumn(name="customer_id") private Customer customer;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=40) private ChatState state=ChatState.MAIN_MENU;
 @Column(columnDefinition="text") private String contextJson="{}";
 @Column(nullable=false) private OffsetDateTime lastInteraction=OffsetDateTime.now();
 public Long getId(){return id;} public void setId(Long v){id=v;}
 public String getPhone(){return phone;} public void setPhone(String v){phone=v;}
 public Customer getCustomer(){return customer;} public void setCustomer(Customer v){customer=v;}
 public ChatState getState(){return state;} public void setState(ChatState v){state=v;}
 public String getContextJson(){return contextJson;} public void setContextJson(String v){contextJson=v;}
 public OffsetDateTime getLastInteraction(){return lastInteraction;} public void setLastInteraction(OffsetDateTime v){lastInteraction=v;}
}
