package miles.identigate.soja.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by ADMIN on 9/13/2015.
 */
public class TextViewBold extends android.support.v7.widget.AppCompatTextView {
    public TextViewBold(Context context) {
        super(context);
        setFont();
    }
    public TextViewBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public TextViewBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Semibold.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}