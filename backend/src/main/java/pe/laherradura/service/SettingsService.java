package pe.laherradura.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.laherradura.entity.BusinessSetting;
import pe.laherradura.repository.BusinessSettingRepository;

import java.math.BigDecimal;
import java.time.LocalTime;

@Service
@Transactional
public class SettingsService {
    private final BusinessSettingRepository repository;

    public SettingsService(BusinessSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public BusinessSetting get() {
        return normalize(repository.findById(1L).orElseGet(BusinessSetting::new));
    }

    public BusinessSetting save(BusinessSetting settings) {
        settings.setId(1L);
        return repository.save(normalize(settings));
    }

    private BusinessSetting normalize(BusinessSetting settings) {
        if (blank(settings.getBusinessName())) settings.setBusinessName("Carnicería La Herradura");
        if (blank(settings.getAssistantName())) settings.setAssistantName("Mashico");
        if (blank(settings.getTimeZone())) settings.setTimeZone("America/Lima");
        if (blank(settings.getAttentionDays())) settings.setAttentionDays("MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY");
        if (settings.getOpeningTime() == null) settings.setOpeningTime(LocalTime.of(8, 0));
        if (settings.getClosingTime() == null) settings.setClosingTime(LocalTime.of(20, 0));
        if (settings.getSameDayCutoffTime() == null) settings.setSameDayCutoffTime(LocalTime.of(19, 30));
        if (blank(settings.getReservationSlots())) settings.setReservationSlots("08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00");
        if (settings.getMinimumDeliveryAmount() == null) settings.setMinimumDeliveryAmount(BigDecimal.ZERO);
        if (blank(settings.getCurrency())) settings.setCurrency("PEN");
        return settings;
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }
}
