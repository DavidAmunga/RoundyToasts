package miles.identigate.soja.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by ADMIN on 9/13/2015.
 */
public class OpenSansBold extends android.support.v7.widget.AppCompatTextView {
    public OpenSansBold(Context context) {
        super(context);
        setFont();
    }
    public OpenSansBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public OpenSansBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Semibold.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}
