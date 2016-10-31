package miles.identigate.soja.Services;

import android.app.Activity;
import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.DriveIn;
import miles.identigate.soja.R;

public class SyncService extends IntentService {
    public static final String NOTIFICATION="miles.identigate.soja";
    private int result= Activity.RESULT_CANCELED;
    public static final String MESSAGE="MESSAGE";
    public static final String RESULT="RESULT";
    private DatabaseHandler handler;
    NotificationManager manager;
    NotificationCompat.Builder builder;
    int NOTIFICATION_ID = 5050;
    public static int currentIndex=0;
    private Preferences preferences;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    /*
    * 0=drive in
    * 1=walk in
    * */
    private static int currentType=0;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     */
    public SyncService() {
        super("SyncService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler=new DatabaseHandler(this);
        preferences=new Preferences(this);
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        builder = new NotificationCompat.Builder(this);
        builder.setContentTitle("Soja")
                .setContentText("Sync in progress")
                .setSmallIcon(R.mipmap.ic_launcher);
        Bundle extras = intent.getExtras();
        boolean isNetworkConnected = extras.getBoolean("isNetworkConnected");
        if(isNetworkConnected){
            //TODO send offline data to server
            driveIns=handler.getUnSyncedDriveIns(0);
            walkIns=handler.getUnSyncedDriveIns(1);
            if(driveIns.size()>0){
                syncDriveIns(currentType);
            }else if(driveIns.size()==0){
                //No drive in data,go to walk in
                if(walkIns.size()>0){
                    currentType=1;
                    syncDriveIns(currentType);
                }
            }
            result=Activity.RESULT_OK;
            publishResults("Success",result);
        }else{
            publishResults("offline",result);
        }

    }
    private void publishResults(String message,int result){
        Intent intent=new Intent(NOTIFICATION);
        intent.putExtra(MESSAGE,message);
        intent.putExtra(RESULT,result);
        sendBroadcast(intent);
    }
    public void showProgressNotification(int max,int progress){
        // Sets the progress indicator to a max value, the
        // current completion percentage, and "determinate"
        // state
        int percent=(progress/max)*100;
        builder.setProgress(100, percent, false);
        // Displays the progress bar for the first time.
        manager.notify(NOTIFICATION_ID, builder.build());
        // Sleeps the thread, simulating an operation
        // that takes time
    }
    /*
    * 0=drive in
    * 1=walk in
    * */
    public void syncDriveIns(int type){
        if (type == 0) {
            recordDriveIn(driveIns.get(currentIndex),"drive-in");
            showProgressNotification(driveIns.size(),currentIndex);
        }else if(type==1){
            recordDriveIn(walkIns.get(currentIndex),"walk-in");
            showProgressNotification(walkIns.size(),currentIndex);
        }
    }
    public void recordDriveIn(DriveIn in,String visitType) {
        String urlParameters = null;
        try {
            String idN=in.getNationalId();
            String gender=in.getSex().contains("M")?"0":"1";
            urlParameters =
                            "visitType=" + URLEncoder.encode(visitType, "UTF-8") +
                                    "&deviceID=" + URLEncoder.encode(preferences.getDeviceId(), "UTF-8")+
                                    "&premiseZoneID=" + URLEncoder.encode(preferences.getPremise(), "UTF-8")+
                                    "&visitorTypeID=" + URLEncoder.encode(in.getVisitorType(), "UTF-8")+
                                    "&houseID=" + URLEncoder.encode(in.getHouseID(), "UTF-8")+
                                    "&entryTime=" + URLEncoder.encode(in.getEntryTime(), "UTF-8")+
                                    "&vehicleRegNO=" + URLEncoder.encode(in.getCarNumber(), "UTF-8")+
                                    "&birthDate=" + URLEncoder.encode(in.getDob(), "UTF-8")+
                                    "&genderID=" + URLEncoder.encode(gender, "UTF-8")+
                                    "&firstName=" + URLEncoder.encode(in.getName(), "UTF-8")+
                                    "&lastName=" + URLEncoder.encode(in.getOtherNames(), "UTF-8")+
                                    "&idType=" + URLEncoder.encode(in.getIdType(), "UTF-8")+
                                    "&idNumber=" + URLEncoder.encode(idN.substring(2, idN.length()-1), "UTF-8");
            new DriveinAsync().execute(Constants.BASE_URL + "record-visit", urlParameters);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(

            );
        }
    }
    private class DriveinAsync extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            return new NetworkHandler().excutePost(params[0],params[1]);
        }
        protected void onPostExecute(String result) {
            if(result !=null){
                try {
                    JSONObject obj=new JSONObject(result);
                    int resultCode=obj.getInt("result_code");
                    String resultText=obj.getString("result_text");
                    String resultContent=obj.getString("result_content");
                    if(resultText.equals("OK")&&resultContent.equals("success")){
                        //Record next
                        goToNext();
                    }else {
                       //Skip
                        goToNext();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else{
               //Skip
                goToNext();
            }
        }
    }
    private void goToNext(){
        if(currentType==0){
            ++currentIndex;
            if(currentIndex==driveIns.size()){
                currentType=1;
                currentIndex=0;
                syncDriveIns(currentType);
            }else{
                syncDriveIns(currentType);
            }
        }else if(currentType==1){
            ++currentIndex;
            if(currentIndex==walkIns.size()){
                //Sync walk ins Next
                // When the loop is finished, updates the notification
                builder.setContentText("Sync is complete")
                        // Removes the progress bar
                        .setProgress(0, 0, false);
                manager.notify(NOTIFICATION_ID, builder.build());
            }else{
                syncDriveIns(currentType);
            }
        }

    }
}
