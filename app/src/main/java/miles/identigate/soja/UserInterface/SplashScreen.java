package miles.identigate.soja.UserInterface;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.FirebaseDatabase;

import io.fabric.sdk.android.Fabric;
import miles.identigate.soja.Dashboard;
import miles.identigate.soja.activities.ScanTicket;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.R;

public class SplashScreen extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 2000;


    TextView edtVersion;
    ImageView imgLogo;
    Animation scale_up, slide_up, fade_in;
    TextView soja_name, soja_slogan;
    Preferences preferences;

    private static final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        preferences = new Preferences(this);
        if (preferences.isDarkModeOn()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);


        edtVersion = findViewById(R.id.version_number);
        imgLogo = findViewById(R.id.imgLogo);
        soja_name = findViewById(R.id.soja_name);
        soja_slogan = findViewById(R.id.soja_slogan);

        scale_up = AnimationUtils.loadAnimation(this, R.anim.scale_up);
        slide_up = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        fade_in = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        imgLogo.setAnimation(scale_up);
        soja_name.setAnimation(slide_up);
        soja_slogan.setAnimation(fade_in);

//        Get Version Name
        PackageInfo pinfo = null;
        try {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);
//            int versionNumber = pinfo.versionCode;
            String versionName = pinfo.versionName;

            edtVersion.setText("v." + versionName);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        edtVersion.setAnimation(fade_in);


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                Log.d(TAG, "run: Base URL " + preferences.getBaseURL());

                if (preferences.getBaseURL().toLowerCase().contains("sportpesa")) {
                    FirebaseDatabase.getInstance().getReference(Common.TICKETS).keepSynced(true);

                    Intent i = new Intent(SplashScreen.this, ScanTicket.class);
                    startActivity(i);
                    finish();
                } else {
                    Intent i = new Intent(SplashScreen.this, Dashboard.class);
                    startActivity(i);
                    finish();
                }

                // close this activity
            }
        }, SPLASH_TIME_OUT);
    }

}
