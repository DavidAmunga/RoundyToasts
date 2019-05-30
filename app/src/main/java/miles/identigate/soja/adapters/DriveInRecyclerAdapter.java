package miles.identigate.soja.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;

/**
 * Created by myles on 10/28/15.
 */
public class DriveInRecyclerAdapter extends RecyclerView.Adapter<DriveInRecyclerAdapter.DriveInViewHolder> {
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
    OnItemClick mCallback;

    private static final String TAG = "DriveInAdapter";


    public DriveInRecyclerAdapter() {

    }

    public DriveInRecyclerAdapter(Activity activity, ArrayList<DriveIn> driveIns, int dummy, OnItemClick listener) {
        this.activity = activity;
        type = "DRIVE";
        this.driveIns = driveIns;
        driveInsSearch = new ArrayList<>();
        driveInsSearch.addAll(this.driveIns);
        //Log.e("SIZE: ",driveInsSearch.size()+": "+this.driveIns.size());
        this.mCallback = listener;
    }

    public DriveInRecyclerAdapter(Activity activity, ArrayList<DriveIn> walkIns, String type, OnItemClick listener) {
        this.activity = activity;
        this.type = "WALK";
        this.walkIns = walkIns;
        driveInsSearch = new ArrayList<>();
        driveInsSearch.addAll(this.walkIns);
        this.mCallback = listener;


    }

    public DriveInRecyclerAdapter(Activity activity, String type, ArrayList<ServiceProviderModel> serviceProviderModels, OnItemClick listener) {
        this.activity = activity;
        this.type = "PROVIDER";
        this.serviceProviderModels = serviceProviderModels;
        serviceProviderModelsSearch = new ArrayList<>();
        serviceProviderModelsSearch.addAll(this.serviceProviderModels);
        this.mCallback = listener;

    }

    public DriveInRecyclerAdapter(Activity activity, ArrayList<Resident> residents, OnItemClick listener) {
        this.activity = activity;
        this.type = "RESIDENT";
        this.residents = residents;
        residentsSearch = new ArrayList<>();
        residentsSearch.addAll(this.residents);
        this.mCallback = listener;
    }


    @NonNull
    @Override
    public DriveInViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visitor, parent, false);
        Log.d(TAG, "onCreateViewHolder: Create View");
        return new DriveInViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DriveInViewHolder holder, int position) {

        if (type.equals("DRIVE")) {
            final DriveIn driveIn = driveIns.get(position);

            Log.d(TAG, "Name: " + driveIn.getName());

            holder.name.setText(!driveIns.get(position).getName().equals(" ")
                    && !driveIns.get(position).getName().equals("null") ? driveIns.get(position).getName() : driveIns.get(position).getNationalId());
            holder.idNumber.setText(driveIns.get(position).getCarNumber());
//            holder.e.setText(formatDate(driveIns.get(position).getEntryTime(), "date"));
            holder.entryTime.setText(formatDate(driveIns.get(position).getEntryTime(), "time"));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(driveIn);

                }
            });


            //entry.setText("ENTRY: "+driveIns.get(position).getEntryTime());
        } else if (type.equals("WALK")) {

            final DriveIn walkIn = walkIns.get(position);
            Log.d(TAG, "getView: " + walkIn.getName());
            holder.name.setText(!walkIns.get(position).getName().equals(" ")
                    && !walkIns.get(position).getName().equals("null") ? walkIns.get(position).getName() : walkIns.get(position).getNationalId());

            holder.idNumber.setText(!walkIns.get(position).getName().equals(" ")
                    && !walkIns.get(position).getName().equals("null") ? walkIns.get(position).getNationalId() : "SMS Login");


//            entry_date.setText(formatDate(walkIns.get(position).getEntryTime(), "date"));
            holder.entryTime.setText(formatDate(walkIns.get(position).getEntryTime(), "time"));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(walkIn);

                }
            });


        } else if (type.equals("PROVIDER")) {
            final ServiceProviderModel model = serviceProviderModels.get(position);
            holder.name.setText(model.getCompanyName());
            holder.idNumber.setText("ID: " + serviceProviderModels.get(position).getNationalId());
//            entry_date.setText("ENTRY: " + formatDate(serviceProviderModels.get(position).getEntryTime(), "date"));
            holder.entryTime.setText("ENTRY: " + formatDate(serviceProviderModels.get(position).getEntryTime(), "time"));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(model);

                }
            });


        } else if (type.equals("RESIDENT")) {
            final Resident resident = residents.get(position);
            holder.name.setText(resident.getName());
            holder.idNumber.setText("ID: " + resident.getNationalId());
            holder.entryTime.setText(formatDate(resident.getEntryTime(), "time"));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(resident);

                }
            });


        }
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


    public void removeItem(int position) {
        if (type.equals("DRIVE")) {
            driveIns.remove(position);
            notifyItemRemoved(position);
        } else if (type.equals("WALK")) {
            walkIns.remove(position);
            notifyItemRemoved(position);

        } else if (type.equals("PROVIDER")) {
            serviceProviderModels.remove(position);
            notifyItemRemoved(position);

        } else if (type.equals("RESIDENT")) {
            residents.remove(position);
            notifyItemRemoved(position);

        }

    }

    public void restoreItem(Object object, int position) {
        if (type.equals("DRIVE")) {
            DriveIn driveIn = (DriveIn) object;
            driveIns.add(driveIn);
            notifyItemInserted(position);
        } else if (type.equals("WALK")) {
            DriveIn walkIn = (DriveIn) object;
            walkIns.add(walkIn);
            notifyItemInserted(position);
        } else if (type.equals("PROVIDER")) {
            ServiceProviderModel model = (ServiceProviderModel) object;
            serviceProviderModels.add(model);
            notifyItemInserted(position);
        } else if (type.equals("RESIDENT")) {
            Resident resident = (Resident) object;
            residents.add(resident);
            notifyItemInserted(position);

        }

    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
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


    public class DriveInViewHolder extends RecyclerView.ViewHolder {

        public TextView name, idNumber, entryTime;
        public RelativeLayout viewBackground, viewForeground;

        public DriveInViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            idNumber = itemView.findViewById(R.id.idNumber);
            entryTime = itemView.findViewById(R.id.entry_time);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }
    }

}
