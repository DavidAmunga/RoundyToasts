package miles.identigate.soja.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;


public class FingerprintAdapter extends RecyclerView.Adapter<FingerprintAdapter.ViewHolder> {
    ArrayList<Visitor> items;
    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView name;

        public ViewHolder(View view){
            super(view);
            name=(TextView)view.findViewById(R.id.name);
        }
    }
    public FingerprintAdapter(ArrayList<Visitor> items){
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
        Visitor base = items.get(position);
        holder.name.setText(base.getName());
    }
    @Override
    public int getItemCount() {
        return items.size();
    }


}
