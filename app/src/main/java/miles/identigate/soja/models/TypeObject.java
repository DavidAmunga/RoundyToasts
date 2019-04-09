package miles.identigate.soja.models;

import ir.mirrajabi.searchdialog.core.Searchable;

/**
 * Created by myles on 2/5/16.
 */
public class TypeObject implements Searchable {
    public TypeObject(){

    }
    public TypeObject(String id,String name){
        this.id =  id;
        this.name = name;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    String name;

    @Override
    public String getTitle() {
        return getName();
    }
}
