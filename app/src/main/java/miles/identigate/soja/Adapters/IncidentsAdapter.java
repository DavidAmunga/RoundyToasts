package miles.identigate.soja.Adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.IncidentModel;
import miles.identigate.soja.R;

/**
 * Created by myles on 1/21/16.
 */
public class IncidentsAdapter extends BaseAdapter {
    Activity activity;
    ArrayList<IncidentModel> models;
    public IncidentsAdapter(Activity activity,ArrayList<IncidentModel> models){
        this.activity=activity;
        this.models=models;
    }
    @Override
    public int getCount() {
        return models.size();
    }

    @Override
    public Object getItem(int position) {
        return models.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=activity.getLayoutInflater().inflate(R.layout.incident,null);
        TextView name=(TextView)view.findViewById(R.id.name);
        TextView id=(TextView)view.findViewById(R.id.idNumber);
        TextView type=(TextView)view.findViewById(R.id.type);
        TextView date=(TextView)view.findViewById(R.id.date);
        TextView descr=(TextView)view.findViewById(R.id.description);
        name.setText(models.get(position).getName());
        id.setText("ID: "+models.get(position).getNationalId());
        type.setText("TYPE: "+models.get(position).getCategory());
        date.setText("DATE: "+models.get(position).getDate());
        descr.setText(models.get(position).getDescription());
        return view;
    }
}
