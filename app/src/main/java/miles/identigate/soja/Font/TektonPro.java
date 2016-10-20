package miles.identigate.soja.Font;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/*Custom textview to implement the Tekton Pro font*/
public class TektonPro extends TextView {
    public TektonPro(Context context) {
        super(context);
        setFont();
    }
    public TektonPro(Context context, AttributeSet attrs) {
        super(context, attrs);
        setFont();
    }
    public TektonPro(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFont();
    }

    private void setFont() {
        Typeface font = Typeface.createFromAsset(getContext().getAssets(), "fonts/TektonPro-Bold.otf");
        setTypeface(font, Typeface.NORMAL);
    }
}
