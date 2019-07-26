package com.amunga.roundytoasts;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    public void failedToast(View view) {
        new FailedToast(this, "This is Failure");
    }

    public void successToast(View view) {
        new SuccessToast(this, "This is Success");

    }

    public void infoToast(View view) {
        new InfoToast(this, "This is an Info");

    }
}
