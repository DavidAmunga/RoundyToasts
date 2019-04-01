package miles.identigate.soja.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatEditText;
import android.util.AttributeSet;

public class EditTextRegular extends AppCompatEditText {
    public EditTextRegular(Context context) {
        super(context);
        setFont();
    }
    public EditTextRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public EditTextRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}
