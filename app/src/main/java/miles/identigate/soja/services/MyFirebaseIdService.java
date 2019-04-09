package miles.identigate.soja.services;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;

import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.Token;
import miles.identigate.soja.app.Common;

public class MyFirebaseIdService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseIdService";
    Preferences preferences;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    Log.w(TAG, "getInstanceId failed", task.getException());
                    return;
                }
// Get new Instance ID token
                String token = task.getResult().getToken();


                updateTokenToServer(token);


            }
        });


    }


    private void updateTokenToServer(String refreshedToken) {
        preferences=new Preferences(getApplicationContext());

        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference(Common.TOKENS);

        Token token = new Token(refreshedToken);
        if (preferences.getPremiseZoneId() != null) {
//            If logged in update token
            tokens.child(preferences.getPremiseZoneId()).setValue(token);
        }
    }


}
