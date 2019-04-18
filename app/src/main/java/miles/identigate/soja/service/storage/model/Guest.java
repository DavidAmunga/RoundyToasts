package miles.identigate.soja.service.storage.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import miles.identigate.soja.helpers.TimestampConverter;

@Entity(tableName = "guest")
public class Guest implements Parcelable {

    @PrimaryKey()
    @ColumnInfo(name = "premise_id")
    @SerializedName("premise_id")
    @Expose
    @NonNull
    private String premiseId;
    @ColumnInfo(name = "peoplerecord_id")
    @SerializedName("peoplerecord_id")
    @Expose
    private String peoplerecordId;
    @ColumnInfo(name = "house_id")
    @SerializedName("house_id")
    @Expose
    private String houseId;
    @ColumnInfo(name = "premise_zone_id")
    @SerializedName("premise_zone_id")
    @Expose
    private String premiseZoneId;
    @ColumnInfo(name = "host_id")
    @SerializedName("host_id")
    @Expose
    private String hostId;
    @ColumnInfo(name = "qr_token")
    @SerializedName("qr_token")
    @Expose
    @NonNull
    private String qrToken;
    @ColumnInfo(name = "first_name")
    @SerializedName("first_name")
    @Expose
    private String firstName;
    @ColumnInfo(name = "last_name")
    @SerializedName("last_name")
    @Expose
    private String lastName;
    @ColumnInfo(name = "phone")
    @SerializedName("phone")
    @Expose
    private String phone;
    @ColumnInfo(name = "email")
    @SerializedName("email")
    @Expose
    private String email;
    @ColumnInfo(name = "id_number")
    @SerializedName("id_number")
    @Expose
    private String idNumber;
    @ColumnInfo(name = "id_type")
    @SerializedName("id_type")
    @Expose
    private String idType;
    @ColumnInfo(name = "house_description")
    @SerializedName("house_description")
    @Expose
    private String houseDescription;
    @ColumnInfo(name = "premise_zone")
    @SerializedName("premise_zone")
    @Expose
    private String premiseZone;
    @ColumnInfo(name = "premise")
    @SerializedName("premise")
    @Expose
    private String premise;
    @ColumnInfo(name = "gender")
    @SerializedName("gender")
    @Expose
    private String gender;
    @ColumnInfo(name = "designation")
    @SerializedName("designation")
    @Expose
    private String designation;
    @ColumnInfo(name = "company")
    @SerializedName("company")
    @Expose
    private String company;


    public final static Parcelable.Creator<Guest> CREATOR = new Creator<Guest>() {


        @SuppressWarnings({
                "unchecked"
        })
        public Guest createFromParcel(Parcel in) {
            return new Guest(in);
        }

        public Guest[] newArray(int size) {
            return (new Guest[size]);
        }

    };


    //    Use for ordering items in view
// use for ordering the items in view
    public static DiffUtil.ItemCallback<Guest> DIFF_CALLBACK = new DiffUtil.ItemCallback<Guest>() {
        @Override
        public boolean areItemsTheSame(@NonNull Guest oldItem, @NonNull Guest newItem) {
            return oldItem.getPeoplerecordId().equals(newItem.getPeoplerecordId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Guest oldItem, @NonNull Guest newItem) {
            return oldItem.getPeoplerecordId().equals(newItem.getPeoplerecordId());
        }
    };


    protected Guest(Parcel in) {
        this.premiseId = ((String) in.readValue((String.class.getClassLoader())));
        this.peoplerecordId = ((String) in.readValue((String.class.getClassLoader())));
        this.houseId = ((String) in.readValue((String.class.getClassLoader())));
        this.premiseZoneId = ((String) in.readValue((String.class.getClassLoader())));
        this.hostId = ((String) in.readValue((String.class.getClassLoader())));
        this.qrToken = ((String) in.readValue((Object.class.getClassLoader())));
        this.firstName = ((String) in.readValue((String.class.getClassLoader())));
        this.lastName = ((String) in.readValue((String.class.getClassLoader())));
        this.phone = ((String) in.readValue((String.class.getClassLoader())));
        this.email = ((String) in.readValue((String.class.getClassLoader())));
        this.idNumber = ((String) in.readValue((String.class.getClassLoader())));
        this.idType = ((String) in.readValue((String.class.getClassLoader())));
        this.houseDescription = ((String) in.readValue((String.class.getClassLoader())));
        this.premiseZone = ((String) in.readValue((String.class.getClassLoader())));
        this.premise = ((String) in.readValue((String.class.getClassLoader())));
        this.gender = ((String) in.readValue((String.class.getClassLoader())));
        this.company = ((String) in.readValue((String.class.getClassLoader())));
        this.designation = ((String) in.readValue((String.class.getClassLoader())));
    }

    public Guest() {
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getPremiseId() {
        return premiseId;
    }

    public void setPremiseId(String premiseId) {
        this.premiseId = premiseId;
    }

    public String getPeoplerecordId() {
        return peoplerecordId;
    }

    public void setPeoplerecordId(String peoplerecordId) {
        this.peoplerecordId = peoplerecordId;
    }

    public String getHouseId() {
        return houseId;
    }

    public void setHouseId(String houseId) {
        this.houseId = houseId;
    }

    public String getPremiseZoneId() {
        return premiseZoneId;
    }

    public void setPremiseZoneId(String premiseZoneId) {
        this.premiseZoneId = premiseZoneId;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
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

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getHouseDescription() {
        return houseDescription;
    }

    public void setHouseDescription(String houseDescription) {
        this.houseDescription = houseDescription;
    }

    public String getPremiseZone() {
        return premiseZone;
    }

    public void setPremiseZone(String premiseZone) {
        this.premiseZone = premiseZone;
    }

    public String getPremise() {
        return premise;
    }

    public void setPremise(String premise) {
        this.premise = premise;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(premiseId);
        dest.writeValue(peoplerecordId);
        dest.writeValue(houseId);
        dest.writeValue(premiseZoneId);
        dest.writeValue(hostId);
        dest.writeValue(qrToken);
        dest.writeValue(firstName);
        dest.writeValue(lastName);
        dest.writeValue(phone);
        dest.writeValue(email);
        dest.writeValue(idNumber);
        dest.writeValue(idType);
        dest.writeValue(houseDescription);
        dest.writeValue(premiseZone);
        dest.writeValue(premise);
        dest.writeValue(gender);
    }

    public int describeContents() {
        return 0;
    }

}