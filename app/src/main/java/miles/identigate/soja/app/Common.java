/**
 * Copyright ,2016 Identigate Inc.
 * bdhobare@gmail.com
 **/
package miles.identigate.soja.app;

import android.app.Application;
import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by myles on 4/24/16.
 */
public class Common extends Application {
    ///Class constants
    public static final int DRIVE_IN=0;
    public static final int WALK_IN=1;
    public static final int SERVICE_PROVIDER=2;
    public static final int RESIDENTS=3;
    public static final int INCIDENT=4;

    //Entry types
    public static final int SCAN=0;
    public static final int MANUAL=1;
    public Context context;

    //some public variables for scanned data:used in passing data from ScanActivity to the relevant activity
    public static final String DOB="DOB";
    public static final String SEX="SEX";
    public static final String FIRST_NAME="FIRST_NAME";
    public static final String OTHER_NAMES="OTHER_NAMES";
    public static final String ID_TYPE="ID_TYPE";
    public static final String ID_NUMBER="ID_NUMBER";

    @Override
    public void onCreate(){
        super.onCreate();
        context=getApplicationContext();
    }
}
