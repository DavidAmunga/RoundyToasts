package miles.identigate.soja.Adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.Models.Resident;
import miles.identigate.soja.Models.ServiceProviderModel;
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

    //Double lists for Search
    ArrayList<DriveIn> driveInsSearch;
    ArrayList<ServiceProviderModel> serviceProviderModelsSearch;
    ArrayList<Resident> residentsSearch;
    //private boolean firstTime = true;

    private static final String TAG = "DriveInAdapter";


    public DriveInAdapter() {

    }

    public DriveInAdapter(Activity activity, ArrayList<DriveIn> driveIns, int dummy) {
        this.activity = activity;
        type = "DRIVE";
        this.driveIns = driveIns;
        driveInsSearch = new ArrayList<>();
        driveInsSearch.addAll(this.driveIns);
        //Log.e("SIZE: ",driveInsSearch.size()+": "+this.driveIns.size());
    }

    public DriveInAdapter(Activity activity, ArrayList<DriveIn> walkIns, String type) {
        this.activity = activity;
        this.type = "WALK";
        this.walkIns = walkIns;
        driveInsSearch = new ArrayList<>();
        driveInsSearch.addAll(this.walkIns);
    }

    public DriveInAdapter(Activity activity, String type, ArrayList<ServiceProviderModel> serviceProviderModels) {
        this.activity = activity;
        this.type = "PROVIDER";
        this.serviceProviderModels = serviceProviderModels;
        serviceProviderModelsSearch = new ArrayList<>();
        serviceProviderModelsSearch.addAll(this.serviceProviderModels);
    }

    public DriveInAdapter(Activity activity, ArrayList<Resident> residents) {
        this.activity = activity;
        this.type = "RESIDENT";
        this.residents = residents;
        residentsSearch = new ArrayList<>();
        residentsSearch.addAll(this.residents);
    }

    @Override
    public void notifyDataSetChanged() {
        //reloadData();
        super.notifyDataSetChanged();
    }

    public void reloadData() {
        if (type.equals("DRIVE")) {
            driveInsSearch = new ArrayList<>();
            driveInsSearch.addAll(this.driveIns);
            //driveIns.addAll(driveInsSearch);
        } else if (type.equals("WALK")) {
            driveInsSearch = new ArrayList<>();
            driveInsSearch.addAll(this.walkIns);
        } else if (type.equals("PROVIDER")) {
            serviceProviderModelsSearch = new ArrayList<>();
            serviceProviderModelsSearch.addAll(this.serviceProviderModels);
        } else if (type.equals("RESIDENT")) {
            residentsSearch = new ArrayList<>();
            residentsSearch.addAll(this.residents);
        }
        //firstTime=false;
    }

    @Override
    public int getCount() {
        if (type.equals("DRIVE")) {
            return driveIns.size();
        } else if (type.equals("WALK")) {
            return walkIns.size();
        } else if (type.equals("PROVIDER")) {
            return serviceProviderModels.size();
        } else if (type.equals("RESIDENT")) {
            return residents.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (type.equals("DRIVE")) {
            return driveIns.get(position);
        } else if (type.equals("WALK")) {
            return walkIns.get(position);
        } else if (type.equals("PROVIDER")) {
            return serviceProviderModels.get(position);
        } else if (type.equals("RESIDENT")) {
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
        View view = activity.getLayoutInflater().inflate(R.layout.visitor, null);
        TextView name = view.findViewById(R.id.name);
        TextView id = view.findViewById(R.id.idNumber);
        TextView car = view.findViewById(R.id.car);
        TextView entry_date = view.findViewById(R.id.entry_date);
        TextView entry_time = view.findViewById(R.id.entry_time);
        if (type.equals("DRIVE")) {
            Log.d(TAG, "Name: " + driveIns.get(position).getName());

            name.setText(!driveIns.get(position).getName().equals(" ")
                    && !driveIns.get(position).getName().equals("null") ? driveIns.get(position).getName() : driveIns.get(position).getNationalId());
            id.setText(driveIns.get(position).getCarNumber());
            entry_date.setText(formatDate(driveIns.get(position).getEntryTime(), "date"));
            entry_time.setText(formatDate(driveIns.get(position).getEntryTime(), "time"));


            //entry.setText("ENTRY: "+driveIns.get(position).getEntryTime());
        } else if (type.equals("WALK")) {
            Log.d(TAG, "getView: " + walkIns.get(position).getName());
            name.setText(!walkIns.get(position).getName().equals(" ")
                    && !walkIns.get(position).getName().equals("null") ? walkIns.get(position).getName() : walkIns.get(position).getNationalId());

            id.setText(!walkIns.get(position).getName().equals(" ")
                    && !walkIns.get(position).getName().equals("null") ? walkIns.get(position).getNationalId() : "SMS Login");


            entry_date.setText(formatDate(walkIns.get(position).getEntryTime(), "date"));
            entry_time.setText(formatDate(walkIns.get(position).getEntryTime(), "time"));

        } else if (type.equals("PROVIDER")) {
            name.setText(serviceProviderModels.get(position).getCompanyName());
            id.setText("ID: " + serviceProviderModels.get(position).getNationalId());
            entry_date.setText("ENTRY: " + formatDate(serviceProviderModels.get(position).getEntryTime(), "date"));
            entry_time.setText("ENTRY: " + formatDate(serviceProviderModels.get(position).getEntryTime(), "time"));
        } else if (type.equals("RESIDENT")) {
            name.setText(residents.get(position).getName());
            id.setText("ID: " + residents.get(position).getNationalId());
            entry_time.setText(formatDate(residents.get(position).getEntryTime(), "time"));

        }
        return view;
    }

    public String formatDate(String date, String type) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date oldDate = format.parse(date);
            String newDate;
            if (type.equals("date")) {
                newDate = new SimpleDateFormat("dd MMMM yyyy").format(oldDate);
            } else {
                newDate = new SimpleDateFormat("h:mm a").format(oldDate);

            }
            return newDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "Date";

    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        clear();
        if (charText.length() == 0) {
            addAll();
        } else {
            search(charText);
        }
        notifyDataSetChanged();
    }

    private void search(String charText) {
        Log.d(TAG, "search: " + type);
        if (type.equals("DRIVE")) {
            for (DriveIn wp : driveInsSearch) {
                if (wp.getCarNumber().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getName().toLowerCase(Locale.getDefault()).contains(charText) ||
                        wp.getNationalId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    driveIns.add(wp);
                }
            }
        } else if (type.equals("WALK")) {
            for (DriveIn wp : driveInsSearch) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText) || wp.getNationalId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    walkIns.add(wp);
                }
            }
        } else if (type.equals("PROVIDER")) {
            for (ServiceProviderModel wp : serviceProviderModelsSearch) {
                if (wp.getCompanyName().toLowerCase(Locale.getDefault()).contains(charText) || wp.getNationalId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    serviceProviderModels.add(wp);
                }
            }
        } else if (type.equals("RESIDENT")) {
            for (Resident wp : residentsSearch) {
                if (wp.getName().toLowerCase(Locale.getDefault()).contains(charText) || wp.getNationalId().toLowerCase(Locale.getDefault()).contains(charText)) {
                    residents.add(wp);
                }
            }
        }
    }

    private void addAll() {
        if (type.equals("DRIVE")) {
            driveIns.addAll(driveInsSearch);
        } else if (type.equals("WALK")) {
            walkIns.addAll(driveInsSearch);
        } else if (type.equals("PROVIDER")) {
            serviceProviderModels.addAll(serviceProviderModelsSearch);
        } else if (type.equals("RESIDENT")) {
            residents.addAll(residentsSearch);
        }
        notifyDataSetChanged();
    }

    private void clear() {
        if (type.equals("DRIVE")) {
            driveIns.clear();
        } else if (type.equals("WALK")) {
            walkIns.clear();
        } else if (type.equals("PROVIDER")) {
            serviceProviderModels.clear();
        } else if (type.equals("RESIDENT")) {
            residents.clear();
        }
    }
}
