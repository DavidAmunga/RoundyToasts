package miles.identigate.soja.models;

import java.util.Arrays;

/**
 * Created by myles on 10/28/15.
 */
public class Visitor  {

    public byte[] getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(byte[] fingerprint, int len) {
        this.fingerprint = Arrays.copyOf(fingerprint, len);
    }

    byte[] fingerprint;
    private String national_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String id;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private String type;

    public String getEntry_time() {
        return entry_time;
    }

    public void setEntry_time(String entry_time) {
        this.entry_time = entry_time;
    }

    private String entry_time;

    public String getCar_reg() {
        return car_reg;
    }

    public void setCar_reg(String car_reg) {
        this.car_reg = car_reg;
    }

    private String car_reg;

    public String getNational_id() {
        return national_id;
    }

    public void setNational_id(String national_id) {
        this.national_id = national_id;
    }

    public String getTo_see() {
        return to_see;
    }

    public void setTo_see(String to_see) {
        this.to_see = to_see;
    }

    private String to_see;

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    private String car_type;

    public String getCar_model() {
        return car_model;
    }

    public void setCar_model(String car_model) {
        this.car_model = car_model;
    }

    private String car_model;

    public String getCar_color() {
        return car_color;
    }

    public void setCar_color(String car_color) {
        this.car_color = car_color;
    }

    private String car_color;

    public String getExit_time() {
        return exit_time;
    }

    public void setExit_time(String exit_time) {
        this.exit_time = exit_time;
    }

    private String exit_time;

    public String getTransit() {
        return transit;
    }

    public void setTransit(String transit) {
        this.transit = transit;
    }

    private String transit;
}
