package pe.laherradura.service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.repository.BusinessSettingRepository;
@Service @Transactional
public class SettingsService {
 private final BusinessSettingRepository repo;
 public SettingsService(BusinessSettingRepository r){repo=r;}
 @Transactional(readOnly=true) public BusinessSetting get(){return repo.findById(1L).orElseGet(BusinessSetting::new);}
 public BusinessSetting save(BusinessSetting s){s.setId(1L);return repo.save(s);}
}
