package miles.identigate.soja.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.PremiseResident;
import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;


public class FingerprintAdapter extends RecyclerView.Adapter<FingerprintAdapter.ViewHolder> {
    ArrayList<PremiseResident> items;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;
        private TextView idNumber;

        public ViewHolder(View view){
            super(view);
            name = view.findViewById(R.id.name);
            idNumber = view.findViewById(R.id.idNumber);
        }
    }
    public FingerprintAdapter(ArrayList<PremiseResident> items){
        this.items=items;
    }
    @Override
    public FingerprintAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fingerprint_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PremiseResident base = items.get(position);
        holder.name.setText(base.getFirstName() + " " + base.getLastName());
        holder.idNumber.setText(base.getIdNumber());
    }
    @Override
    public int getItemCount() {
        return items.size();
    }


}
