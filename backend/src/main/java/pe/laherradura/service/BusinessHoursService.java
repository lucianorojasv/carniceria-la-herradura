package pe.laherradura.service;

import org.springframework.stereotype.Service;
import pe.laherradura.entity.BusinessSetting;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class BusinessHoursService {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", new Locale("es", "PE"));

    public Status status(BusinessSetting settings) {
        ZoneId zone = zone(settings);
        ZonedDateTime now = ZonedDateTime.now(zone);
        boolean businessDay = businessDays(settings).contains(now.getDayOfWeek());
        LocalTime opening = valueOr(settings.getOpeningTime(), LocalTime.of(8, 0));
        LocalTime closing = valueOr(settings.getClosingTime(), LocalTime.of(20, 0));
        LocalTime cutoff = valueOr(settings.getSameDayCutoffTime(), closing);
        boolean open = businessDay && !now.toLocalTime().isBefore(opening) && now.toLocalTime().isBefore(closing);
        boolean acceptsSameDay = open && !now.toLocalTime().isAfter(cutoff);
        return new Status(now, open, acceptsSameDay, nextBusinessDate(settings, now.toLocalDate()));
    }

    public LocalDate nextBusinessDate(BusinessSetting settings, LocalDate fromDate) {
        Set<DayOfWeek> days = businessDays(settings);
        LocalDate candidate = fromDate.plusDays(1);
        for (int i = 0; i < 14; i++) {
            if (days.contains(candidate.getDayOfWeek())) return candidate;
            candidate = candidate.plusDays(1);
        }
        return fromDate.plusDays(1);
    }

    public List<String> reservationSlots(BusinessSetting settings) {
        String raw = settings.getReservationSlots();
        if (raw == null || raw.isBlank()) raw = "08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00";
        List<String> result = new ArrayList<>();
        Arrays.stream(raw.split("[;\\n]+"))
                .map(String::trim)
                .filter(value -> value.matches("\\d{2}:\\d{2}\\s*-\\s*\\d{2}:\\d{2}"))
                .map(value -> value.replaceAll("\\s+", ""))
                .forEach(result::add);
        return result;
    }

    public OffsetDateTime scheduledStart(BusinessSetting settings, LocalDate date, String slot) {
        String start = slot.split("-")[0].trim();
        LocalTime time = LocalTime.parse(start);
        return date.atTime(time).atZone(zone(settings)).toOffsetDateTime();
    }

    public String humanDate(LocalDate date) {
        String text = DATE_FORMAT.format(date);
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    public String scheduleDescription(BusinessSetting settings) {
        LocalTime opening = valueOr(settings.getOpeningTime(), LocalTime.of(8, 0));
        LocalTime closing = valueOr(settings.getClosingTime(), LocalTime.of(20, 0));
        LocalTime cutoff = valueOr(settings.getSameDayCutoffTime(), closing);
        return daysDescription(settings) + " de " + opening + " a " + closing
                + ". Pedidos para el mismo día hasta las " + cutoff + ".";
    }

    private String daysDescription(BusinessSetting settings) {
        Set<DayOfWeek> days = businessDays(settings);
        if (days.size() == 7) return "Todos los días";
        return days.stream()
                .map(day -> day.getDisplayName(TextStyle.FULL, new Locale("es", "PE")))
                .map(name -> name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1))
                .reduce((a, b) -> a + ", " + b)
                .orElse("Días por configurar");
    }

    private Set<DayOfWeek> businessDays(BusinessSetting settings) {
        EnumSet<DayOfWeek> result = EnumSet.noneOf(DayOfWeek.class);
        String raw = settings.getAttentionDays();
        if (raw != null) {
            for (String token : raw.split(",")) {
                try {
                    result.add(DayOfWeek.valueOf(token.trim().toUpperCase(Locale.ROOT)));
                } catch (Exception ignored) {
                    // Ignora días inválidos y conserva los válidos.
                }
            }
        }
        if (result.isEmpty()) result.addAll(EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY));
        return result;
    }

    private ZoneId zone(BusinessSetting settings) {
        try {
            return ZoneId.of(settings.getTimeZone());
        } catch (Exception ignored) {
            return ZoneId.of("America/Lima");
        }
    }

    private LocalTime valueOr(LocalTime value, LocalTime fallback) {
        return value == null ? fallback : value;
    }

    public record Status(ZonedDateTime now, boolean open, boolean acceptsSameDay, LocalDate nextBusinessDate) { }
}
