package miles.identigate.soja.services;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.models.DriveIn;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class VisitorsService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_FETCH_VISITORS = "miles.identigate.soja.Services.action.ACTION_FETCH_VISITORS";
    DatabaseHandler handler;


    public VisitorsService() {
        super("VisitorsService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void ACTION_FETCH_VISITORS(Context context) {
        Intent intent = new Intent(context, VisitorsService.class);
        intent.setAction(ACTION_FETCH_VISITORS);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        handler=new DatabaseHandler(this);
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_FETCH_VISITORS.equals(action)) {
                handleACTION_FETCH_VISITORS();
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleACTION_FETCH_VISITORS() {
        // TODO: Handle action Foo
    }
    private class GetActiveVisitors extends AsyncTask<String,Void,String> {

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }
        @Override
        public void onPostExecute(String s){
            if (s != null){
                //Log.e("Result",s);
                Object json=null;
                try {
                    json=new JSONTokener(s).nextValue();
                    if (json instanceof JSONObject){
                        JSONObject object=new JSONObject(s);
                        JSONArray array=object.getJSONArray("result_content");
                        if (array.length() >0 ){
                            for (int i=0;i<array.length();i++){
                                JSONObject item=array.getJSONObject(i);
                                String name=item.getString("fullname");
                                String id=item.getString("id_number");
                                String entry=item.getString("entry_time");
                                String house=item.getString("house");
                                if(!item.isNull("registration")){
                                    DriveIn driveIn=new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    if (!item.isNull("registration")){
                                        driveIn.setCarNumber(item.getString("registration"));
                                        handler.insertDriveIn(driveIn);
                                    }
                                }else{
                                    DriveIn driveIn=new DriveIn();
                                    driveIn.setName(name);
                                    driveIn.setNationalId(id);
                                    driveIn.setEntryTime(entry);
                                    if (item.isNull("registration")){
                                        handler.insertDriveIn(driveIn);
                                    }
                                }
                            }
                            //Insert data to SQLite
                        }else{

                        }
                    }else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                //No logs
            }

        }
    }

}
