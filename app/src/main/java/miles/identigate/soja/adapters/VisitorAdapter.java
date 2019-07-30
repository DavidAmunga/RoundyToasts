package miles.identigate.soja.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import miles.identigate.soja.models.Visitor;

/**
 * Created by myles on 10/28/15.
 */
public class VisitorAdapter extends RecyclerView.Adapter<VisitorAdapter.VisitorViewHolder> {
    Activity activity;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;

    ArrayList<Visitor> visitors;
    ArrayList<Visitor> visitorSearch;
    //Double lists for Search
    ArrayList<DriveIn> driveInsSearch;
    ArrayList<ServiceProviderModel> serviceProviderModelsSearch;
    ArrayList<Resident> residentsSearch;
    //private boolean firstTime = true;
    OnItemClick mCallback;

    private static final String TAG = "DriveInAdapter";


    public VisitorAdapter() {

    }

    public VisitorAdapter(Activity activity, ArrayList<Visitor> visitors, OnItemClick listener) {
        this.activity = activity;
        this.visitors = visitors;
        visitorSearch = new ArrayList<>();
        visitorSearch.addAll(this.visitors);
        //Log.e("SIZE: ",driveInsSearch.size()+": "+this.driveIns.size());
        this.mCallback = listener;
    }


//
//    public VisitorAdapter(Activity activity, ArrayList<DriveIn> driveIns, int dummy, OnItemClick listener) {
//        this.activity = activity;
//        type = "DRIVE";
//        this.driveIns = driveIns;
//        driveInsSearch = new ArrayList<>();
//        driveInsSearch.addAll(this.driveIns);
//        //Log.e("SIZE: ",driveInsSearch.size()+": "+this.driveIns.size());
//        this.mCallback = listener;
//    }
//
//    public VisitorAdapter(Activity activity, ArrayList<DriveIn> walkIns, String type, OnItemClick listener) {
//        this.activity = activity;
//        this.type = "WALK";
//        this.walkIns = walkIns;
//        driveInsSearch = new ArrayList<>();
//        driveInsSearch.addAll(this.walkIns);
//        this.mCallback = listener;
//
//
//    }
//
//    public VisitorAdapter(Activity activity, String type, ArrayList<ServiceProviderModel> serviceProviderModels, OnItemClick listener) {
//        this.activity = activity;
//        this.type = "PROVIDER";
//        this.serviceProviderModels = serviceProviderModels;
//        serviceProviderModelsSearch = new ArrayList<>();
//        serviceProviderModelsSearch.addAll(this.serviceProviderModels);
//        this.mCallback = listener;
//
//    }
//
//    public VisitorAdapter(Activity activity, ArrayList<Resident> residents, OnItemClick listener) {
//        this.activity = activity;
//        this.type = "RESIDENT";
//        this.residents = residents;
//        residentsSearch = new ArrayList<>();
//        residentsSearch.addAll(this.residents);
//        this.mCallback = listener;
//    }


    @NonNull
    @Override
    public VisitorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.visitor, parent, false);
        Log.d(TAG, "onCreateViewHolder: Create View");
        return new VisitorViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VisitorViewHolder holder, int position) {

        Visitor visitor = visitors.get(position);

        if (visitor.getCar_reg() != null && !visitor.getCar_reg().isEmpty()) {


            Log.d(TAG, "Name: " + visitor.getName());

            holder.name.setText(!visitors.get(position).getName().equals(" ")
                    && !visitors.get(position).getName().equals("null") ? visitors.get(position).getName() : visitors.get(position).getNational_id());
            holder.idNumber.setText(visitors.get(position).getCar_reg());
//            holder.e.setText(formatDate(driveIns.get(position).getEntryTime(), "date"));
            holder.entryTime.setText(formatDate(visitors.get(position).getEntry_time(), "time"));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(visitor);

                }
            });


            //entry.setText("ENTRY: "+driveIns.get(position).getEntryTime());
        } else {

            Log.d(TAG, "getView: " + visitor.getName());
            holder.name.setText(!visitors.get(position).getName().equals(" ")
                    && !visitors.get(position).getName().equals("null") ? visitors.get(position).getName() : visitors.get(position).getNational_id());

            holder.idNumber.setText(!visitors.get(position).getName().equals(" ")
                    && !visitors.get(position).getName().equals("null") ? visitors.get(position).getNational_id() : "SMS Login");


//            entry_date.setText(formatDate(walkIns.get(position).getEntryTime(), "date"));
            holder.entryTime.setText(formatDate(visitors.get(position).getEntry_time(), "time"));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onVisitorClick(visitor);

                }
            });


        }


    }


    public void reloadData() {
        visitorSearch = new ArrayList<>();
        visitorSearch.addAll(this.visitors);

        //firstTime=false;
    }


    public void removeItem(int position) {

        visitorSearch.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Object object, int position) {
        Visitor visitor = (Visitor) object;
        visitors.add(visitor);
        notifyItemInserted(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return visitors.size();
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
        for (Visitor visitor : visitorSearch) {
            if (visitor.getName().toLowerCase(Locale.getDefault()).contains(charText) || visitor.getNational_id().toLowerCase(Locale.getDefault()).contains(charText)) {
                visitors.add(visitor);
            }
        }

    }

    private void addAll() {
        visitors.addAll(visitorSearch);
        notifyDataSetChanged();
    }

    private void clear() {
        visitors.clear();
    }


    public class VisitorViewHolder extends RecyclerView.ViewHolder {

        public TextView name, idNumber, entryTime, visitor_type;
        public RelativeLayout viewBackground, viewForeground;

        public VisitorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            idNumber = itemView.findViewById(R.id.idNumber);
            visitor_type = itemView.findViewById(R.id.visitor_type);
            entryTime = itemView.findViewById(R.id.entry_time);
            viewBackground = itemView.findViewById(R.id.view_background);
            viewForeground = itemView.findViewById(R.id.view_foreground);
        }
    }

}
