package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.WhatsAppMessageLog;
public interface WhatsAppMessageLogRepository extends JpaRepository<WhatsAppMessageLog,String> {}
