package pe.laherradura.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "business_settings")
public class BusinessSetting {
    @Id
    private Long id = 1L;

    @Column(nullable = false, length = 160)
    private String businessName = "Carnicería La Herradura";

    @Column(length = 30)
    private String phone = "938149352";

    @Column(length = 300)
    private String address = "[PENDIENTE]";

    @Column(length = 300)
    private String openingHours = "Lunes a sábado: 08:00 a 20:00";

    @Column(nullable = false)
    private boolean deliveryEnabled = true;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minimumDeliveryAmount = BigDecimal.valueOf(50);

    @Column(nullable = false, length = 8)
    private String currency = "PEN";

    @Column(length = 500)
    private String welcomeMessage = "Calidad, frescura y sabor para tu mesa";

    @Column(nullable = false, length = 80)
    private String assistantName = "Mashico";

    @Column(nullable = false, length = 80)
    private String timeZone = "America/Lima";

    @Column(nullable = false, length = 120)
    private String attentionDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY";

    @Column(nullable = false)
    private LocalTime openingTime = LocalTime.of(8, 0);

    @Column(nullable = false)
    private LocalTime closingTime = LocalTime.of(20, 0);

    @Column(nullable = false)
    private LocalTime sameDayCutoffTime = LocalTime.of(19, 30);

    @Column(nullable = false)
    private boolean allowNextDayReservations = true;

    @Column(nullable = false, length = 500)
    private String reservationSlots = "08:00-10:00;10:00-12:00;12:00-14:00;14:00-16:00;16:00-18:00";

    @Column(nullable = false)
    private boolean sendProductImages = true;

    @Column(nullable = false)
    private boolean yapeEnabled = false;

    @Column(length = 30)
    private String yapeNumber;

    @Column(length = 160)
    private String yapeHolder;

    @Column(length = 800)
    private String yapeQrUrl;

    @Column(nullable = false)
    private boolean plinEnabled = false;

    @Column(length = 30)
    private String plinNumber;

    @Column(length = 160)
    private String plinHolder;

    @Column(length = 800)
    private String plinQrUrl;

    @Column(nullable = false)
    private boolean transferEnabled = false;

    @Column(length = 100)
    private String bankName;

    @Column(length = 80)
    private String bankAccountType;

    @Column(length = 80)
    private String bankAccountNumber;

    @Column(length = 80)
    private String bankCci;

    @Column(length = 160)
    private String bankHolder;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getOpeningHours() { return openingHours; }
    public void setOpeningHours(String openingHours) { this.openingHours = openingHours; }
    public boolean isDeliveryEnabled() { return deliveryEnabled; }
    public void setDeliveryEnabled(boolean deliveryEnabled) { this.deliveryEnabled = deliveryEnabled; }
    public BigDecimal getMinimumDeliveryAmount() { return minimumDeliveryAmount; }
    public void setMinimumDeliveryAmount(BigDecimal minimumDeliveryAmount) { this.minimumDeliveryAmount = minimumDeliveryAmount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public void setWelcomeMessage(String welcomeMessage) { this.welcomeMessage = welcomeMessage; }
    public String getAssistantName() { return assistantName; }
    public void setAssistantName(String assistantName) { this.assistantName = assistantName; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
    public String getAttentionDays() { return attentionDays; }
    public void setAttentionDays(String attentionDays) { this.attentionDays = attentionDays; }
    public LocalTime getOpeningTime() { return openingTime; }
    public void setOpeningTime(LocalTime openingTime) { this.openingTime = openingTime; }
    public LocalTime getClosingTime() { return closingTime; }
    public void setClosingTime(LocalTime closingTime) { this.closingTime = closingTime; }
    public LocalTime getSameDayCutoffTime() { return sameDayCutoffTime; }
    public void setSameDayCutoffTime(LocalTime sameDayCutoffTime) { this.sameDayCutoffTime = sameDayCutoffTime; }
    public boolean isAllowNextDayReservations() { return allowNextDayReservations; }
    public void setAllowNextDayReservations(boolean allowNextDayReservations) { this.allowNextDayReservations = allowNextDayReservations; }
    public String getReservationSlots() { return reservationSlots; }
    public void setReservationSlots(String reservationSlots) { this.reservationSlots = reservationSlots; }
    public boolean isSendProductImages() { return sendProductImages; }
    public void setSendProductImages(boolean sendProductImages) { this.sendProductImages = sendProductImages; }
    public boolean isYapeEnabled() { return yapeEnabled; }
    public void setYapeEnabled(boolean yapeEnabled) { this.yapeEnabled = yapeEnabled; }
    public String getYapeNumber() { return yapeNumber; }
    public void setYapeNumber(String yapeNumber) { this.yapeNumber = yapeNumber; }
    public String getYapeHolder() { return yapeHolder; }
    public void setYapeHolder(String yapeHolder) { this.yapeHolder = yapeHolder; }
    public String getYapeQrUrl() { return yapeQrUrl; }
    public void setYapeQrUrl(String yapeQrUrl) { this.yapeQrUrl = yapeQrUrl; }
    public boolean isPlinEnabled() { return plinEnabled; }
    public void setPlinEnabled(boolean plinEnabled) { this.plinEnabled = plinEnabled; }
    public String getPlinNumber() { return plinNumber; }
    public void setPlinNumber(String plinNumber) { this.plinNumber = plinNumber; }
    public String getPlinHolder() { return plinHolder; }
    public void setPlinHolder(String plinHolder) { this.plinHolder = plinHolder; }
    public String getPlinQrUrl() { return plinQrUrl; }
    public void setPlinQrUrl(String plinQrUrl) { this.plinQrUrl = plinQrUrl; }
    public boolean isTransferEnabled() { return transferEnabled; }
    public void setTransferEnabled(boolean transferEnabled) { this.transferEnabled = transferEnabled; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public String getBankAccountType() { return bankAccountType; }
    public void setBankAccountType(String bankAccountType) { this.bankAccountType = bankAccountType; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String bankAccountNumber) { this.bankAccountNumber = bankAccountNumber; }
    public String getBankCci() { return bankCci; }
    public void setBankCci(String bankCci) { this.bankCci = bankCci; }
    public String getBankHolder() { return bankHolder; }
    public void setBankHolder(String bankHolder) { this.bankHolder = bankHolder; }
}
