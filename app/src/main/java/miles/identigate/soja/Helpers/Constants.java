package miles.identigate.soja.Helpers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by myles on 10/30/15.
 */
public class Constants {
    public static String BASE_URL="https://soja.co.ke/soja-rest/api/visits/";
    public static String GET_VISITORS_URL="https://soja.co.ke/soja-rest/api/visitors/visitors_in/";
    public static final String WALK="WALKING";
    public static final String DRIVE="DRIVING";
    public static HashMap<String,String> fieldItems=new HashMap<>();
    public static String getCurrentTimeStamp(){
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String string=simpleDateFormat.format(now);
        return string;
    }
    public static String formatDate(String date){
        Date temp = null;
        try {
            //TODO change to timestamp
            temp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp.toString();
    }
}
