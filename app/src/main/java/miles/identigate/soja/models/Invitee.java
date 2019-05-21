package miles.identigate.soja.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Invitee implements Parcelable {

    @SerializedName("booking_id")
    @Expose
    private String bookingId;
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @SerializedName("phone")
    @Expose
    private String phone;
    @SerializedName("email")
    @Expose
    private String email;
    @SerializedName("booking_reason")
    @Expose
    private String bookingReason;
    @SerializedName("arrival_date")
    @Expose
    private String arrivalDate;
    @SerializedName("arrival_time")
    @Expose
    private String arrivalTime;
    @SerializedName("destination")
    @Expose
    private String destination;
    @SerializedName("host_pr_id")
    @Expose
    private String hostPrId;
    @SerializedName("house_id")
    @Expose
    private String houseId;
    @SerializedName("host_first_name")
    @Expose
    private String hostFirstName;
    @SerializedName("host_last_name")
    @Expose
    private String hostLastName;
    public final static Parcelable.Creator<Invitee> CREATOR = new Creator<Invitee>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Invitee createFromParcel(Parcel in) {
            return new Invitee(in);
        }

        public Invitee[] newArray(int size) {
            return (new Invitee[size]);
        }

    };

    protected Invitee(Parcel in) {
        this.bookingId = ((String) in.readValue((String.class.getClassLoader())));
        this.firstName = ((String) in.readValue((String.class.getClassLoader())));
        this.lastName = ((String) in.readValue((String.class.getClassLoader())));
        this.phone = ((String) in.readValue((String.class.getClassLoader())));
        this.email = ((String) in.readValue((String.class.getClassLoader())));
        this.bookingReason = ((String) in.readValue((String.class.getClassLoader())));
        this.arrivalDate = ((String) in.readValue((String.class.getClassLoader())));
        this.arrivalTime = ((String) in.readValue((String.class.getClassLoader())));
        this.destination = ((String) in.readValue((String.class.getClassLoader())));
        this.hostPrId = ((String) in.readValue((String.class.getClassLoader())));
        this.houseId = ((String) in.readValue((String.class.getClassLoader())));
        this.hostFirstName = ((String) in.readValue((String.class.getClassLoader())));
        this.hostLastName = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Invitee() {
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getBookingReason() {
        return bookingReason;
    }

    public void setBookingReason(String bookingReason) {
        this.bookingReason = bookingReason;
    }

    public String getArrivalDate() {
        return arrivalDate;
    }

    public void setArrivalDate(String arrivalDate) {
        this.arrivalDate = arrivalDate;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getHostPrId() {
        return hostPrId;
    }

    public void setHostPrId(String hostPrId) {
        this.hostPrId = hostPrId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getHostFirstName() {
        return hostFirstName;
    }

    public void setHostFirstName(String hostFirstName) {
        this.hostFirstName = hostFirstName;
    }

    public String getHostLastName() {
        return hostLastName;
    }

    public void setHostLastName(String hostLastName) {
        this.hostLastName = hostLastName;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(bookingId);
        dest.writeValue(firstName);
        dest.writeValue(lastName);
        dest.writeValue(phone);
        dest.writeValue(email);
        dest.writeValue(bookingReason);
        dest.writeValue(arrivalDate);
        dest.writeValue(arrivalTime);
        dest.writeValue(destination);
        dest.writeValue(hostPrId);
        dest.writeValue(houseId);
        dest.writeValue(hostFirstName);
        dest.writeValue(hostLastName);
    }

    public int describeContents() {
        return 0;
    }

}