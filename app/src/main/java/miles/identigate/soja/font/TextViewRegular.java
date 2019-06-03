package miles.identigate.soja.font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

public class TextViewRegular extends android.support.v7.widget.AppCompatTextView {
    public TextViewRegular(Context context) {
        super(context);
        setFont();
    }
    public TextViewRegular(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public TextViewRegular(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/OpenSans-Regular.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}