package miles.identigate.soja.models;

public class Ticket {
    private String entryTime;
    private String premiseZoneId;
    private String deviceId;
    private String ticketId;


    public Ticket() {
    }

    public Ticket(String entryTime, String premiseZoneId, String deviceId, String ticketId) {
        this.entryTime = entryTime;
        this.premiseZoneId = premiseZoneId;
        this.deviceId = deviceId;
        this.ticketId = ticketId;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getPremiseZoneId() {
        return premiseZoneId;
    }

    public void setPremiseZoneId(String premiseZoneId) {
        this.premiseZoneId = premiseZoneId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }
}
