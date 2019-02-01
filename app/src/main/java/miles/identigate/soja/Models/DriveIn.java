package miles.identigate.soja.Models;

/**
 * Created by myles on 1/19/16.
 */
public class DriveIn {
    public String getOtherNames() {
        return otherNames;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    String otherNames;

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    String dob;

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    String sex;

    public String getIdType() {
        return idType;
    }

    public void setIdType(String idType) {
        this.idType = idType;
    }

    String idType;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    String nationalId;

    public String getVisitorType() {
        return visitorType;
    }

    public void setVisitorType(String visitorType) {
        this.visitorType = visitorType;
    }

    String visitorType;

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    String carNumber;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    String image;

    public String getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(String entryTime) {
        this.entryTime = entryTime;
    }

    String entryTime;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    String status;

    public String getExitTime() {
        return exitTime;
    }

    public void setExitTime(String exitTime) {
        this.exitTime = exitTime;
    }

    String exitTime;

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    String recordType;

    public boolean isSynced() {
        return synced;
    }

    public void setSynced(boolean synced) {
        this.synced = synced;
    }

    boolean synced;

    public String getHouseID() {
        return houseID;
    }

    public void setHouseID(String houseID) {
        this.houseID = houseID;
    }

    String houseID;
}
