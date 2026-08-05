package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.dto.PromotionRequest;
import pe.laherradura.entity.Promotion;
import pe.laherradura.exception.NotFoundException;
import pe.laherradura.repository.PromotionRepository;
import java.util.List;
@Service @Transactional
public class PromotionService {
 private final PromotionRepository repo;
 public PromotionService(PromotionRepository r){repo=r;}
 @Transactional(readOnly=true) public List<Promotion> list(boolean active){return active?repo.findByActiveTrueOrderByStartDateDesc():repo.findAll();}
 public Promotion save(Long id,PromotionRequest r){Promotion p=id==null?new Promotion():repo.findById(id).orElseThrow(()->new NotFoundException("Promoción no encontrada"));p.setName(r.name());p.setDescription(r.description());p.setPromotionalPrice(r.promotionalPrice());p.setStartDate(r.startDate());p.setEndDate(r.endDate());p.setActive(r.active());p.setImageUrl(r.imageUrl());return repo.save(p);}
}
