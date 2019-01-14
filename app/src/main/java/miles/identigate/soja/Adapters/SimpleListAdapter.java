package miles.identigate.soja.Adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;

/**
 * Created by myles on 2/5/16.
 */
public class SimpleListAdapter extends BaseAdapter {
    Activity context;
    ArrayList<String> data;
    LayoutInflater inflater;


    public SimpleListAdapter(Activity context, ArrayList<String> data) {
        this.context=context;
        this.data=data;
        inflater=LayoutInflater.from(context);
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
    public View getView(int position, View view, ViewGroup parent) {
       view=inflater.inflate(R.layout.spinner_list_item,null);
       TextView names=view.findViewById(R.id.textView);
       names.setText(data.get(position));
       return view;
    }

    static class ViewHolder {
        TextView text;
    }
}
