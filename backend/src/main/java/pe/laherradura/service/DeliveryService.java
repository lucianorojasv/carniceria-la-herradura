package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.DeliveryZoneRequest;
import pe.laherradura.entity.DeliveryZone;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.DeliveryZoneRepository;
import java.util.List;
@Service @Transactional
public class DeliveryService {
 private final DeliveryZoneRepository repo;
 public DeliveryService(DeliveryZoneRepository r){repo=r;}
 @Transactional(readOnly=true) public List<DeliveryZone> list(boolean active){return active?repo.findByActiveTrueOrderByNameAsc():repo.findAll();}
 public DeliveryZone save(Long id,DeliveryZoneRequest r){DeliveryZone z=id==null?new DeliveryZone():repo.findById(id).orElseThrow(()->new NotFoundException("Zona no encontrada"));z.setName(r.name().trim());z.setFee(r.fee());z.setMinimumOrder(r.minimumOrder());z.setActive(r.active());return repo.save(z);}
}
