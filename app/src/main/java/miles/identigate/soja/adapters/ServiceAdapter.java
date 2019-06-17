package miles.identigate.soja.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnServiceOptionClick;
import miles.identigate.soja.models.ServiceOption;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ServiceViewholder> {

    private static final String TAG = "ServiceAdapter";

    Context context;
    ArrayList<ServiceOption> serviceOptions;
    OnServiceOptionClick mCallback;

    public ServiceAdapter(Context context, ArrayList<ServiceOption> serviceOptions, OnServiceOptionClick mCallback) {
        this.context = context;
        this.serviceOptions = serviceOptions;
        this.mCallback = mCallback;
    }

    @NonNull
    @Override
    public ServiceViewholder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_layout_item, parent, false);
        Log.d(TAG, "onCreateViewHolder: Create View");
        return new ServiceViewholder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewholder holder, int i) {
        ServiceOption serviceOption = serviceOptions.get(i);

        holder.serviceName.setText(serviceOption.getDescription());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onServiceOptionClick(serviceOption);
            }
        });
    }


    @Override
    public int getItemCount() {
        return serviceOptions.size();
    }

    public class ServiceViewholder extends RecyclerView.ViewHolder {
        TextView serviceName;

        public ServiceViewholder(@NonNull View itemView) {
            super(itemView);
            serviceName = itemView.findViewById(R.id.name);
        }
    }
}
