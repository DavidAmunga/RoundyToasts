package miles.identigate.soja.Adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;

/**
 * Created by myles on 2/5/16.
 */
public class TypeAdapter extends BaseAdapter {
    Activity context;
    int resource;
    ArrayList<TypeObject> data;
    public TypeAdapter(Activity context, int resource, ArrayList<TypeObject> data) {
        this.context=context;
        this.resource=resource;
        this.data=data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder holder;

        if (row == null) {
            LayoutInflater inflater=context.getLayoutInflater();
            row = inflater.inflate(resource, parent, false);
            holder = new ViewHolder();
            holder.text = row.findViewById(R.id.text);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }


        TypeObject item = data.get(position);
        holder.text.setText(item.getName());
        return row;
    }

    static class ViewHolder {
        TextView text;
    }
}
