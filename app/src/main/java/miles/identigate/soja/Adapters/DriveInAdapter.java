package miles.identigate.soja.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;

/**
 * Created by myles on 10/28/15.
 */
public class DriveInAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    String type;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;
    public DriveInAdapter(){

    }
    public DriveInAdapter(Activity activity,ArrayList<DriveIn> driveIns,int dummy){
        this.activity=activity;
        type="DRIVE";
        this.driveIns=driveIns;
    }
    public DriveInAdapter(Activity activity, ArrayList<DriveIn> walkIns,String type){
        this.activity=activity;
        this.type="WALK";
        this.walkIns=walkIns;
    }
    public DriveInAdapter(Activity activity,String type,ArrayList<ServiceProviderModel> serviceProviderModels){
        this.activity=activity;
        this.type="PROVIDER";
        this.serviceProviderModels=serviceProviderModels;
    }
    public DriveInAdapter(Activity activity, ArrayList<Resident> residents){
        this.activity=activity;
        this.type="RESIDENT";
        this.residents=residents;
    }
    @Override
    public int getCount() {
        if(type.equals("DRIVE")){
            return driveIns.size();
        }else if(type.equals("WALK")){
            return walkIns.size();
        }else if(type.equals("PROVIDER")){
            return serviceProviderModels.size();
        }else if(type.equals("RESIDENT")){
            return residents.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if(type.equals("DRIVE")){
            return driveIns.get(position);
        }else if(type.equals("WALK")){
            return walkIns.get(position);
        }else if(type.equals("PROVIDER")){
            return serviceProviderModels.get(position);
        }else if(type.equals("RESIDENT")){
            return residents.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=activity.getLayoutInflater().inflate(R.layout.visitor,null);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView id=(TextView)view.findViewById(R.id.idNumber);
        TextView car=(TextView)view.findViewById(R.id.car);
        TextView entry=(TextView)view.findViewById(R.id.entry);
        if(type.equals("DRIVE")){
            name.setText(driveIns.get(position).getName());
            id.setText("ID: "+driveIns.get(position).getNationalId());
            car.setText("CAR: " + driveIns.get(position).getCarNumber());
            entry.setText("ENTRY: "+driveIns.get(position).getEntryTime());
        }else if(type.equals("WALK")){
            name.setText(walkIns.get(position).getName());
            id.setText("ID: " + walkIns.get(position).getNationalId());
            car.setVisibility(View.GONE);
            entry.setText("ENTRY: " + walkIns.get(position).getEntryTime());
        }else if(type.equals("PROVIDER")){
            name.setText(serviceProviderModels.get(position).getCompanyName());
            id.setText("ID: "+serviceProviderModels.get(position).getNationalId());
            car.setVisibility(View.GONE);
            entry.setText("ENTRY: " + serviceProviderModels.get(position).getEntryTime());
        }else if(type.equals("RESIDENT")){
            name.setText(residents.get(position).getName());
            id.setText("ID: "+residents.get(position).getNationalId());
            car.setText("HOUSE: "+residents.get(position).getHouse());
            entry.setText("ENTRY: "+residents.get(position).getEntryTime());
        }
        return view;
    }
}
