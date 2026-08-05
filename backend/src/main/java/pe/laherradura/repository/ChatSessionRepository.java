package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.ChatSession;
import java.util.Optional;
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {
    Optional<ChatSession> findByPhone(String phone);
}
