package miles.identigate.soja;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.regula.documentreader.api.results.DocumentReaderTextField;

import java.util.List;

import miles.identigate.soja.Helpers.Constants;

public class SimpleMrzDataAdapter extends ArrayAdapter<DocumentReaderTextField> {
	public SimpleMrzDataAdapter(Context context, int resource, List<DocumentReaderTextField> objects) {
		super(context, resource, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		if (v == null) {

			LayoutInflater vi;
			vi = LayoutInflater.from(getContext());
			v = vi.inflate(R.layout.simple_data_layout, null);
		}

		DocumentReaderTextField p = getItem(position);

		if (p != null) {
            TextView name = v.findViewById(R.id.nameTv);
            TextView textValue = v.findViewById(R.id.valueTv);
            LinearLayout layout = v.findViewById(R.id.simpleItemLayout);

			textValue.setTypeface(Typeface.MONOSPACE);

			/*if (p.values.size() > 0)
			    name.setText(p.values.get(0).value);*/
            String value = Constants.documentReaderResults.getTextFieldValueByType(p.fieldType, p.lcid);
			textValue.setText(value);

            textValue.setTextColor(Color.rgb(3, 140, 7));

			layout.setBackgroundColor(position % 2 > 0 ? Color.rgb(228, 228, 237) : Color.rgb(237, 237, 228));
		}

		return v;
	}
}
