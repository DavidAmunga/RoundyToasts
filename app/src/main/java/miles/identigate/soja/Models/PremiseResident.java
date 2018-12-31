package miles.identigate.soja.Models;

public class PremiseResident {
    String idNumber;
    String firstName;
    String lastName;
    String fingerPrint;

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

    int fingerPrintLen;
}
