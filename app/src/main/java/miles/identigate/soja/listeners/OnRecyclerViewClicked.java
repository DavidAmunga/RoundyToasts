package miles.identigate.soja.listeners;

import android.view.View;

public interface OnRecyclerViewClicked {
    void onClick(View view, int position);
    void onLongClick(View view, int position);
}
