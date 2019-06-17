package miles.identigate.soja.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;


public class ServiceOption implements Parcelable {

    @SerializedName("service_id")
    @Expose
    private String serviceId;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("event_id")
    @Expose
    private String eventId;
    @SerializedName("start_time")
    @Expose
    private String startTime;
    @SerializedName("end_time")
    @Expose
    private String endTime;
    @SerializedName("frequency")
    @Expose
    private String frequency;
    public final static Parcelable.Creator<ServiceOption> CREATOR = new Creator<ServiceOption>() {


        @SuppressWarnings({
                "unchecked"
        })
        public ServiceOption createFromParcel(Parcel in) {
            return new ServiceOption(in);
        }

        public ServiceOption[] newArray(int size) {
            return (new ServiceOption[size]);
        }

    };

    protected ServiceOption(Parcel in) {
        this.serviceId = ((String) in.readValue((String.class.getClassLoader())));
        this.description = ((String) in.readValue((String.class.getClassLoader())));
        this.eventId = ((String) in.readValue((String.class.getClassLoader())));
        this.startTime = ((String) in.readValue((String.class.getClassLoader())));
        this.endTime = ((String) in.readValue((String.class.getClassLoader())));
        this.frequency = ((String) in.readValue((String.class.getClassLoader())));
    }

    /**
     * No args constructor for use in serialization
     */
    public ServiceOption() {
    }

    /**
     * @param startTime
     * @param eventId
     * @param serviceId
     * @param description
     * @param frequency
     * @param endTime
     */
    public ServiceOption(String serviceId, String description, String eventId, String startTime, String endTime, String frequency) {
        super();
        this.serviceId = serviceId;
        this.description = description;
        this.eventId = eventId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.frequency = frequency;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(serviceId);
        dest.writeValue(description);
        dest.writeValue(eventId);
        dest.writeValue(startTime);
        dest.writeValue(endTime);
        dest.writeValue(frequency);
    }

    public int describeContents() {
        return 0;
    }

}