package miles.identigate.soja.Helpers;

import android.content.Context;
import android.support.annotation.NonNull;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import miles.identigate.soja.R;

/**
 * Created by myles on 10/30/15.
 */
public class Constants {
    //public static String URL = "http://41.204.190.77/soja-rest/";
    public static String URL = "https://soja.co.ke/soja-rest/";
    public static String BASE_URL= URL + "api/visits/";
    public static String GET_VISITORS_URL= URL + "api/visitors/visitors_in/";

    public static final String LOGOUT_BROADCAST = "miles.identigate.soja.ACTION_LOGOUT";
    public static final String RECORDED_VISITOR = "miles.identigate.soja.RECORDED_VISITOR";
    public static final String EXITED_VISITOR = "miles.identigate.soja.EXITED_VISITOR";

    public static final String WALK="WALKING";
    public static final String DRIVE="DRIVING";
    public static HashMap<String,String> fieldItems=new HashMap<>();
    public static MaterialDialog showProgressDialog(Context context, String title, String content){
        return   new MaterialDialog.Builder(context)
                .title(title)
                .content(content)
                .progress(true, 0)
                .cancelable(false)
                .widgetColorRes(R.color.colorPrimary)
                .build();
    }
    public static MaterialDialog  showDialog(Context context, String title, String content, String positive,MaterialDialog.SingleButtonCallback callback){
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

    public static String getCurrentTimeStamp(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String string=simpleDateFormat.format(now);
        return string;
    }
    public static String timeStamp(){
       return new Date().toString();
    }
}
