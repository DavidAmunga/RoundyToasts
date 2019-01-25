package miles.identigate.soja.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by ADMIN on 9/13/2015.
 */
public class BrandonGrotesqueBold extends android.support.v7.widget.AppCompatTextView {
    public BrandonGrotesqueBold(Context context) {
        super(context);
        setFont();
    }
    public BrandonGrotesqueBold(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public BrandonGrotesqueBold(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/BrandonGrotesque-Bold.ttf");
        setTypeface(font, Typeface.NORMAL);
    }
}
