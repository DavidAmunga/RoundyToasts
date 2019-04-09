package miles.identigate.soja.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.R;

/**
 * Created by myles on 2/5/16.
 */
public class TypeArrayAdapter extends ArrayAdapter {
    Activity context;
    int resource;
    ArrayList<TypeObject> data;

    public TypeArrayAdapter(Activity context, int resource, ArrayList<TypeObject> data) {
        super(context, resource, data);
        this.context = context;
        this.resource = resource;
        this.data = data;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public String getItem(int position) {
        return data.get(position).getName();
    }

//
//    @Nullable
//    @Override
//    public TypeObject getItem(int position) {
//        return super.getItem(position);
//    }

//    @Override
//    public long getItemId(int position) {
//        return position;
//    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view= convertView;

        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view= inflater.inflate(resource, parent, false);
        TextView text = view.findViewById(R.id.text);

//        TypeObject item = data.get(position);
        text.setText(data.get(position).getName());
        return view;
    }

//    static class ViewHolder {
//        TextView text;
//    }
}
