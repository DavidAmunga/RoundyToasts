package miles.identigate.soja.helpers;

import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.regula.documentreader.api.results.DocumentReaderResults;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import miles.identigate.soja.R;

/**
 * Created by myles on 10/30/15.
 */
public class Constants {
    //public static String URL = "http://41.204.190.77/soja-rest/";
    public static String URL = "https://soja.co.ke/soja-rest/index.php/";
    public static String BASE_URL = URL + "api/visits/";
    public static String GET_VISITORS_URL = URL + "api/visitors/visitors_in/";

    public static final String LOGOUT_BROADCAST = "miles.identigate.soja.ACTION_LOGOUT";
    public static final String RECORDED_VISITOR = "miles.identigate.soja.RECORDED_VISITOR";
    public static final String EXITED_VISITOR = "miles.identigate.soja.EXITED_VISITOR";

    public static Location mLastLocation = null;


    public static final String WALK = "WALKING";
    public static final String DRIVE = "DRIVING";
    public static HashMap<String, String> fieldItems = new HashMap<>();
    public static DocumentReaderResults documentReaderResults = null;


    public static final String DATABASE_NAME = "GuestDB";
    public static final int NUMBERS_OF_THREADS = 3;
    public static final int LOADING_PAGE_SIZE = 20;


    public static MaterialDialog showProgressDialog(Context context, String title, String content) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .progressIndeterminateStyle(true)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();
    }

    public static MaterialDialog showDialog(Context context, String title, String content, String positive, MaterialDialog.SingleButtonCallback callback) {
        return new MaterialDialog.Builder(context)
                .title(title)
                .widgetColorRes(R.color.colorPrimary)
                .content(content)
                .positiveText(positive)
                .negativeText("CANCEL")
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .autoDismiss(true)
                .onPositive(callback)
                .build();
    }

    public static String getCurrentTimeStamp() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        String string = simpleDateFormat.format(now);
        return string;
    }

    public static String timeStamp() {
        return new Date().toString();
    }

    public static String sentenceCaseForText(String text) {

        if (text == null) return "";

        int pos = 0;
        boolean capitalize = true;
        StringBuilder sb = new StringBuilder(text);

        while (pos < sb.length()) {

            if (capitalize && !Character.isWhitespace(sb.charAt(pos))) {

                sb.setCharAt(pos, Character.toUpperCase(sb.charAt(pos)));
            } else if (!capitalize && !Character.isWhitespace(sb.charAt(pos))) {

                sb.setCharAt(pos, Character.toLowerCase(sb.charAt(pos)));
            }

            if (sb.charAt(pos) == '.' || (capitalize && Character.isWhitespace(sb.charAt(pos)))) {

                capitalize = true;
            } else {

                capitalize = false;
            }

            pos++;
        }

        return sb.toString();
    }


    public static void setDashboardCheckIn(miles.identigate.soja.helpers.Preferences preferences, ArrayList<String> titles, ArrayList<Integer> drawables, ArrayList<String> descriptions) {
        if (preferences.getBaseURL().contains("casuals")) {

            titles.add("Walk In");


            descriptions.add("Record walking employee");

            drawables.add(R.drawable.ic_walk_in_new);

//            if (preferences.isSMSCheckInEnabled()) {
//                titles.add("SMS Checkin");
//                descriptions.add("Check in a Visitor without an ID");
//                drawables.add(R.drawable.ic_sms_check_in_new);
//
//
//            }
            if (preferences.isFingerprintsEnabled()) {
                titles.add("Biometric Checkin");
                descriptions.add("Check in using biometrics");
                drawables.add(R.drawable.fingerprint);
            }


        } else if (preferences.getBaseURL().contains("events")) {

            titles.add("Register");
            titles.add("Issue Ticket");
            titles.add("Check In");

            descriptions.add("Register a new guest");
            descriptions.add("Issue a Ticket/Badge");
            descriptions.add("Check in a guest");

            drawables.add(R.drawable.ic_walk_in_new);
            drawables.add(R.drawable.ic_tickets);
            drawables.add(R.drawable.ic_qr);


        } else {
            titles.add("Drive In");
            titles.add("Walk In");
            titles.add("Residents");


            descriptions.add("Record driving visitor");
            descriptions.add("Record walking visitor");
            descriptions.add("Check In a Resident");


            drawables.add(R.drawable.ic_drive_in_new);
            drawables.add(R.drawable.ic_walk_in_new);
            drawables.add(R.drawable.ic_resident_icon_new);


            if (preferences.isSMSCheckInEnabled()) {
                titles.add("SMS Checkin");
                descriptions.add("Check in a Visitor without an ID");
                drawables.add(R.drawable.ic_sms_check_in_new);


            }


            if (preferences.isFingerprintsEnabled()) {
                titles.add("Biometric Checkin");
                descriptions.add("Check in using biometrics");
                drawables.add(R.drawable.fingerprint);
            }

            titles.add("Tickets");
            descriptions.add("Scan an Event Ticket");
            drawables.add(R.drawable.ic_scan_icon);



//            Temp Events


        }
    }


    public static void setDashboardCheckOut(Preferences preferences, ArrayList<String> titles, ArrayList<Integer> drawables, ArrayList<String> descriptions) {
        if (preferences.getBaseURL().contains("casuals")) {
            titles.add("Walk Out");


            descriptions.add("Check out an employee on foot");

            drawables.add(R.drawable.ic_walk_in_new);


            if (preferences.isFingerprintsEnabled()) {
                titles.add("Biometric Checkout");
                descriptions.add("Checkout out using biometrics");
                drawables.add(R.drawable.ic_fingerprint);
            }


        } else if (preferences.getBaseURL().contains("events")) {
            titles.add("Check Out");

            descriptions.add("Check out an guest");

            drawables.add(R.drawable.ic_walk_in_new);

        } else {
            titles.add("Express Checkout");
            titles.add("Drive Out");
            titles.add("Walk Out");
            titles.add("Residents");

            descriptions.add("Scan QR to check out visitor");
            descriptions.add("Check out a driving visitor");
            descriptions.add("Check out a visitor on foot");
            descriptions.add("Check out a resident");

            drawables.add(R.drawable.ic_scan_icon);
            drawables.add(R.drawable.ic_drive_in_new);
            drawables.add(R.drawable.ic_walk_in_new);
            drawables.add(R.drawable.ic_resident_icon_new);


            if (preferences.isFingerprintsEnabled()) {
                titles.add("Biometric Checkout");
                descriptions.add("Checkout out using biometrics");
                drawables.add(R.drawable.ic_fingerprint);
            }


        }
    }

}
