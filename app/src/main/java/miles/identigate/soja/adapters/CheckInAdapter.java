package miles.identigate.soja.adapters;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import miles.identigate.soja.R;

/**
 * Created by myles on 10/31/15.
 */
public class CheckInAdapter extends BaseAdapter {
    String[] titles;
    String[] descriptions;
    Integer[] drawables;
    Activity activity;
    String type;

    public CheckInAdapter(Activity activity, String[] titles, String[] descriptions, Integer[] drawables, String type) {
        this.activity = activity;
        this.titles = titles;
        this.descriptions = descriptions;
        this.drawables = drawables;
        this.type = type;
    }


    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public Object getItem(int position) {
        return titles[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = activity.getLayoutInflater().inflate(R.layout.option, null);
        TextView title = view.findViewById(R.id.title);
        TextView description = view.findViewById(R.id.description);
        ImageView image = view.findViewById(R.id.image);


//        title.setMinimumHeight(pxHeight);

//        title.setMinimumHeight(minHeight);
////        description.setMinimumHeight(minHeight);
////        image.setMinimumHeight(minHeight);


        if (type.equals("checkout")) {
            image.setScaleX(-1);
            image.setPadding(0, 0, 10, 0);

        }
        title.setText(titles[position]);
        description.setText(descriptions[position]);
        image.setImageResource(drawables[position]);
        return view;
    }
}
