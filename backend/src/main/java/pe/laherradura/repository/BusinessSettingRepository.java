package pe.laherradura.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.laherradura.entity.BusinessSetting;

public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, Long> {

}
