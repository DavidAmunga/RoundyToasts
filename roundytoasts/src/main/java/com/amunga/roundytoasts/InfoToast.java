package com.amunga.roundytoasts;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InfoToast extends LinearLayout {
    public InfoToast(Context context, String message) {
        super(context);
        show(context, message);
    }

    private void show(Context context, String message) {
        View view = LayoutInflater.from(context).inflate(R.layout.info_toast, this, true);

        TextView textView = view.findViewById(R.id.text);
        textView.setText(message);

        Toast toast = new Toast(context);

        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(view);
        toast.show();
    }
}
