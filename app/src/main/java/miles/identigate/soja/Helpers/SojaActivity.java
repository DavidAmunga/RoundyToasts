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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLoggedin();
    }

    public void isLoggedin() {
        Preferences settings = new Preferences(getApplicationContext());
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
