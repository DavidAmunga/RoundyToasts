package miles.identigate.soja.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by ADMIN on 9/13/2015.
 */
public class OpenSansItalic extends android.support.v7.widget.AppCompatTextView {
    public OpenSansItalic(Context context) {
        super(context);
        setFont();
    }
    public OpenSansItalic(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public OpenSansItalic(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Italic.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}
