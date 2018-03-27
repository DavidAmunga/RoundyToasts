package miles.identigate.soja.Helpers;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import miles.identigate.soja.UserInterface.Login;

/**
 * Created by myles on 2/2/16.
 */
public class SojaActivity extends AppCompatActivity {
    Preferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        settings  = new Preferences(getApplicationContext());
        isLoggedin();
    }

    public void isLoggedin() {

        if (!settings.isLoggedin()) {
            Intent i = new Intent(getApplicationContext(), Login.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        }
    }
    @TargetApi(11)
    public void ActivateHomeButton(){
        getActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
