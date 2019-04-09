package miles.identigate.soja.service.storage.model;


import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.TypeConverters;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

import miles.identigate.soja.helpers.TimestampConverter;

@Entity(tableName = "guest")
public class Guest implements Serializable{

    @PrimaryKey(autoGenerate = true)
    @SerializedName(value = "id")
    @ColumnInfo(name = "id")
    private Integer id;
    @ColumnInfo(name = "firstName")
    @SerializedName(value = "firstName")
    private String firstName;
    @ColumnInfo(name = "lastName")
    @SerializedName(value = "lastName")
    private String lastName;
    @ColumnInfo(name = "email")
    @SerializedName(value = "email")
    private String email;
    @ColumnInfo(name = "idNumber")
    @SerializedName(value = "idNumber")
    private String idNumber;
    @ColumnInfo(name = "scanIdType")
    @SerializedName(value = "scanIdType")
    private String scanIdType;
    @ColumnInfo(name = "phone")
    @SerializedName(value = "phone")
    private String phone;
    @ColumnInfo(name = "visitorType")
    @SerializedName(value = "visitorType")
    private String visitorType;
    @ColumnInfo(name = "designation")
    @SerializedName(value = "designation")
    private String designation;
    @ColumnInfo(name = "company")
    @SerializedName(value = "company")
    private String company;
    @ColumnInfo(name = "city")
    @SerializedName(value = "city")
    private String city;
    @ColumnInfo(name = "country")
    @SerializedName(value = "country")
    private String country;
    @ColumnInfo(name = "status")
    @SerializedName(value = "status")
    private String status;
    @ColumnInfo(name = "entryTime")
    @SerializedName(value = "entryTime")
    @TypeConverters({TimestampConverter.class})
    private Date entryTime;
    @ColumnInfo(name = "exitTime")
    @SerializedName(value = "exitTime")
    @TypeConverters({TimestampConverter.class})
    private Date exitTime;

//    Use for ordering items in view

    public static DiffUtil.ItemCallback<Guest> DIFF_CALLBACK = new DiffUtil.ItemCallback<Guest>() {
        @Override
        public boolean areItemsTheSame(@NonNull Guest oldItem, @NonNull Guest newItem) {
            return oldItem.getId() == (newItem.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Guest oldItem, @NonNull Guest newItem) {
            return oldItem.getId().equals(newItem.getId());
        }
    };


    public Guest() {
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(Date entryTime) {
        this.entryTime = entryTime;
    }

    public Date getExitTime() {
        return exitTime;
    }

    public void setExitTime(Date exitTime) {
        this.exitTime = exitTime;
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

    public String getScanIdType() {
        return scanIdType;
    }

    public void setScanIdType(String scanIdType) {
        this.scanIdType = scanIdType;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getVisitorType() {
        return visitorType;
    }

    public void setVisitorType(String visitorType) {
        this.visitorType = visitorType;
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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
