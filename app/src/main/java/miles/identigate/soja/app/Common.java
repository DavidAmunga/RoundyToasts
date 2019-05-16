/**
 * Copyright ,2016 Identigate Inc.
 * bdhobare@gmail.com
 **/
package miles.identigate.soja.app;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.Token;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.services.FCMClient;
import miles.identigate.soja.services.IFCMService;

/**
 * Created by myles on 4/24/16.
 */
public class Common extends Application {
    public static final String PREF_CURRENT_DRIVER_PASS = "driverPass";
    public static final String PREF_CURRENT_VISIT_ID = "current_visit_id";
    public static final String PREF_CURRENT_PASSENGERS_LIST = "current_passengers";
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
    public static final int REGISTER_GUEST = 5;
    public static final int ISSUE_TICKET = 6;
    public static final int CHECK_IN_GUEST = 7;

    //Entry types
    public static final int SCAN = 0;
    public static final int MANUAL = 1;
    public Context context;


    //    LISTS
    public static final String GUEST_LIST = "GuestList";
    public static final String VISITORS_LIST = "VisitorList";

    public static final Type GUEST_ARRAY_LIST_CLASS_TYPE = (new ArrayList<Guest>()).getClass();


    //some public variables for scanned data:used in passing data from ScanActivity to the relevant activity
    public static final String DOB = "DOB";
    public static final String SEX = "SEX";
    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String OTHER_NAMES = "OTHER_NAMES";
    public static final String ID_TYPE = "ID_TYPE";
    public static final String ID_NUMBER = "ID_NUMBER";

    public static final String fcmURL = "https://fcm.googleapis.com/";


    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        preferences = new Preferences(context);
    }


    public static IFCMService getFCMService() {
        return FCMClient.getClient(fcmURL).create(IFCMService.class);
    }

//    public static void updateFirebaseToken(final Preferences preferences) {
//
//        FirebaseDatabase db = FirebaseDatabase.getInstance();
//
//        final DatabaseReference tokens = db.getReference(Common.TOKENS);
//
//
//        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
//            @Override
//            public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                if (!task.isSuccessful()) {
//                    Log.w(TAG, "getInstanceId failed", task.getException());
//                    return;
//                }
//// Get new Instance ID token
////                String token = task.getResult().getToken();
//
//                Token token = new Token(task.getResult().getToken());
//                tokens.child("sentry_" + preferences.getId()).setValue(token);
//            }
//        });
//
//
//    }

    public static Map<String, Object> getFieldNamesAndValues(final Object obj, boolean publicOnly)
            throws IllegalArgumentException, IllegalAccessException {
        Class<? extends Object> c1 = obj.getClass();
        Map<String, Object> map = new HashMap<String, Object>();
        Field[] fields = c1.getDeclaredFields();
        for (int i = 0; i < fields.length; i++) {
            String name = fields[i].getName();
            if (publicOnly) {
                if (Modifier.isPublic(fields[i].getModifiers())) {
                    Object value = fields[i].get(obj);
                    map.put(name, value);
                }
            } else {
                fields[i].setAccessible(true);
                Log.d(TAG, "getFieldNamesAndValues: " + name);
                Object value = fields[i].get(obj);
                map.put(name, value);
            }
        }
        return map;
    }

    public static String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }

    public static String centerString(int width, String s) {
        String finalText = String.format("%-" + width + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
        Log.d(TAG, "centerString: " + "\n" + finalText);

        return finalText;
    }

    public static String formatString(String item) {
        String finalText = "";


        String[] splitStr = item.split("\\s+");

        Log.d(TAG, "formatString: String " + splitStr);
        Log.d(TAG, "formatString: String Length" + splitStr.length);
        Log.d(TAG, "formatString:  Total String Length " + splitStr[0].length() + splitStr[1].length() + 1);

        if (splitStr[0].length() < 16) {
//            Log.d(TAG, "formatString: Less than 16");
            if (splitStr[1] != null && splitStr[0].length() + splitStr[1].length() + 1 < 16) {
//                Log.d(TAG, "formatString: Less than 16 including second");

                finalText = item + "\n";
                finalText = Common.centerString(16, finalText);

                return finalText;
            } else {
                Log.d(TAG, "formatString: More than 16");

                for (int i = 0; i < 2; i++) {
                    finalText = Common.toProperCase(splitStr[0]) + " " + Common.toProperCase(splitStr[1]).charAt(0) + "." + "\n";
                    finalText = Common.centerString(16, finalText);
                }

                return finalText;
            }
        }


        return finalText;

    }


}
