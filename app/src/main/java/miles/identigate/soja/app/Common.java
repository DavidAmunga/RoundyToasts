/**
 * Copyright ,2016 Identigate Inc.
 * bdhobare@gmail.com
 **/
package miles.identigate.soja.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.Token;
import miles.identigate.soja.Models.TypeObject;
import miles.identigate.soja.R;

/**
 * Created by myles on 4/24/16.
 */
public class Common extends Application {
    Preferences preferences;
    private static final String TAG = "Common";
    public static final String TOKENS = "TOKENS";

    public static final String ENTITY_NAME = "";
    public static final String ENTITY_OWNER = "";

    ///Class constants
    public static final int DRIVE_IN = 0;
    public static final int WALK_IN = 1;
    public static final int SERVICE_PROVIDER = 2;
    public static final int RESIDENTS = 3;
    public static final int INCIDENT = 4;

    //Entry types
    public static final int SCAN = 0;
    public static final int MANUAL = 1;
    public Context context;

    //some public variables for scanned data:used in passing data from ScanActivity to the relevant activity
    public static final String DOB = "DOB";
    public static final String SEX = "SEX";
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String OTHER_NAMES = "OTHER_NAMES";
    public static final String ID_TYPE = "ID_TYPE";
    public static final String ID_NUMBER = "ID_NUMBER";

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
    }

    public static void updateFirebaseToken(final Preferences preferences) {

        FirebaseDatabase db = FirebaseDatabase.getInstance();

        final DatabaseReference tokens = db.getReference(Common.TOKENS);


        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }
// Get new Instance ID token
//                String token = task.getResult().getToken();

                Token token = new Token(task.getResult().getToken());
                tokens.child(preferences.getPremiseZoneId()).setValue(token);
            }
        });


    }


}
