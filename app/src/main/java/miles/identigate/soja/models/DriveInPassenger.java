package miles.identigate.soja.models;

import android.os.Parcel;
import android.os.Parcelable;

public class DriveInPassenger implements Parcelable {
    private String phone;
    private String companyName;
    private String house;
    private String firstName;
    private String scan_id_type;
    private String visitType;
    private String deviceID;
    private String premiseZoneID;
    private String visitorTypeID;
    private String houseID;
    private String hostID;
    private int paxinvehicle;
    private String entryTime;
    private String vehicleRegNO;
    private String birthDate;
    private String genderID;
    private String lastName;
    private String idType;
    private String idNumber;
    private String nationality;
    private String nationCode;


    public DriveInPassenger() {
    }

    protected DriveInPassenger(Parcel in) {
        phone = in.readString();
        companyName = in.readString();
        house = in.readString();
        firstName = in.readString();
        scan_id_type = in.readString();
        visitType = in.readString();
        deviceID = in.readString();
        premiseZoneID = in.readString();
        visitorTypeID = in.readString();
        houseID = in.readString();
        hostID = in.readString();
        paxinvehicle = in.readInt();
        entryTime = in.readString();
        vehicleRegNO = in.readString();
        birthDate = in.readString();
        genderID = in.readString();
        lastName = in.readString();
        idType = in.readString();
        idNumber = in.readString();
        nationality = in.readString();
        nationCode = in.readString();
    }

    public static final Creator<DriveInPassenger> CREATOR = new Creator<DriveInPassenger>() {
        @Override
        public DriveInPassenger createFromParcel(Parcel in) {
            return new DriveInPassenger(in);
        }

        @Override
        public DriveInPassenger[] newArray(int size) {
            return new DriveInPassenger[size];
        }
    };

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getScan_id_type() {
        return scan_id_type;
    }

    public void setScan_id_type(String scan_id_type) {
        this.scan_id_type = scan_id_type;
    }

    public String getVisitType() {
        return visitType;
    }

    public void setVisitType(String visitType) {
        this.visitType = visitType;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getPremiseZoneID() {
        return premiseZoneID;
    }

    public void setPremiseZoneID(String premiseZoneID) {
        this.premiseZoneID = premiseZoneID;
    }

    public String getVisitorTypeID() {
        return visitorTypeID;
    }

    public void setVisitorTypeID(String visitorTypeID) {
        this.visitorTypeID = visitorTypeID;
    }

    public String getHouseID() {
        return houseID;
    }

    public void setHouseID(String houseID) {
        this.houseID = houseID;
    }

    public String getHostID() {
        return hostID;
    }

    public void setHostID(String hostID) {
        this.hostID = hostID;
    }

    public int getPaxinvehicle() {
        return paxinvehicle;
    }

    public void setPaxinvehicle(int paxinvehicle) {
        this.paxinvehicle = paxinvehicle;
    }

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    public String getVehicleRegNO() {
        return vehicleRegNO;
    }

    public void setVehicleRegNO(String vehicleRegNO) {
        this.vehicleRegNO = vehicleRegNO;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGenderID() {
        return genderID;
    }

    public void setGenderID(String genderID) {
        this.genderID = genderID;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getNationCode() {
        return nationCode;
    }

    public void setNationCode(String nationCode) {
        this.nationCode = nationCode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(phone);
        dest.writeValue(companyName);
        dest.writeValue(house);
        dest.writeValue(firstName);
        dest.writeValue(scan_id_type);
        dest.writeValue(visitType);
        dest.writeValue(deviceID);
        dest.writeValue(premiseZoneID);
        dest.writeValue(visitorTypeID);
        dest.writeValue(houseID);
        dest.writeValue(hostID);
        dest.writeValue(paxinvehicle);
        dest.writeValue(entryTime);
        dest.writeValue(vehicleRegNO);
        dest.writeValue(birthDate);
        dest.writeValue(genderID);
        dest.writeValue(lastName);
        dest.writeValue(idType);
        dest.writeValue(idNumber);
        dest.writeValue(nationality);
        dest.writeValue(nationCode);

    }
}