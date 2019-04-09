package miles.identigate.soja.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("firstname")
    @Expose
    private String firstname;
    @SerializedName("lastname")
    @Expose
    private String lastname;
    @SerializedName("device_id")
    @Expose
    private String deviceId;
    @SerializedName("active")
    @Expose
    private String active;
    @SerializedName("premise_zone_id")
    @Expose
    private String premiseZoneId;
    @SerializedName("premise_id")
    @Expose
    private String premiseId;
    @SerializedName("premise_name")
    @Expose
    private String premiseName;
    @SerializedName("printable")
    @Expose
    private String printable;
    @SerializedName("organisationID")
    @Expose
    private String organisationID;
    @SerializedName("access_token")
    @Expose
    private String accessToken;
    public final static Parcelable.Creator<User> CREATOR = new Creator<User>() {


        @SuppressWarnings({
                "unchecked"
        })
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return (new User[size]);
        }

    };

    protected User(Parcel in) {
        this.id = ((String) in.readValue((String.class.getClassLoader())));
        this.firstname = ((String) in.readValue((String.class.getClassLoader())));
        this.lastname = ((String) in.readValue((String.class.getClassLoader())));
        this.deviceId = ((String) in.readValue((String.class.getClassLoader())));
        this.active = ((String) in.readValue((String.class.getClassLoader())));
        this.premiseZoneId = ((String) in.readValue((String.class.getClassLoader())));
        this.premiseId = ((String) in.readValue((String.class.getClassLoader())));
        this.premiseName = ((String) in.readValue((String.class.getClassLoader())));
        this.printable = ((String) in.readValue((String.class.getClassLoader())));
        this.organisationID = ((String) in.readValue((String.class.getClassLoader())));
        this.accessToken = ((String) in.readValue((String.class.getClassLoader())));
    }

    public User() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getPremiseZoneId() {
        return premiseZoneId;
    }

    public void setPremiseZoneId(String premiseZoneId) {
        this.premiseZoneId = premiseZoneId;
    }

    public String getPremiseId() {
        return premiseId;
    }

    public void setPremiseId(String premiseId) {
        this.premiseId = premiseId;
    }

    public String getPremiseName() {
        return premiseName;
    }

    public void setPremiseName(String premiseName) {
        this.premiseName = premiseName;
    }

    public String getPrintable() {
        return printable;
    }

    public void setPrintable(String printable) {
        this.printable = printable;
    }

    public String getOrganisationID() {
        return organisationID;
    }

    public void setOrganisationID(String organisationID) {
        this.organisationID = organisationID;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(id);
        dest.writeValue(firstname);
        dest.writeValue(lastname);
        dest.writeValue(deviceId);
        dest.writeValue(active);
        dest.writeValue(premiseZoneId);
        dest.writeValue(premiseId);
        dest.writeValue(premiseName);
        dest.writeValue(printable);
        dest.writeValue(organisationID);
        dest.writeValue(accessToken);
    }

    public int describeContents() {
        return 0;
    }

}
