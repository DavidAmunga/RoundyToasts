package miles.identigate.soja.models;

public class PremiseResident {
    String idNumber;
    String firstName;
    String lastName;
    String fingerPrint;
    String hostType;

    String house;

    public String getHouse() {
        return house;
    }

    public void setHouse(String house) {
        this.house = house;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }

    String hostId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    String id;

    public String getIdNumber() {
        return idNumber;
    }

    public void setIdNumber(String idNumber) {
        this.idNumber = idNumber;
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

    public String getFingerPrint() {
        return fingerPrint;
    }

    public void setFingerPrint(String fingerPrint) {
        this.fingerPrint = fingerPrint;
    }

    public int getFingerPrintLen() {
        return fingerPrintLen;
    }

    public void setFingerPrintLen(int fingerPrintLen) {
        this.fingerPrintLen = fingerPrintLen;
    }

    public String getHostType() {
        return hostType;
    }

    public void setHostType(String hostType) {
        this.hostType = hostType;
    }

    int fingerPrintLen;


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PremiseResident) {
            PremiseResident temp = (PremiseResident) obj;
            if (this.firstName.equals(temp.firstName) && this.lastName.equals(temp.lastName) &&
                    this.idNumber.equals(temp.idNumber) && this.hostId.equals(temp.hostId))
                return true;
        }
        return false;

    }

    @Override
    public int hashCode() {
        return (this.firstName.hashCode() + this.lastName.hashCode() + this.idNumber.hashCode() + this.hostId.hashCode());

    }
}
