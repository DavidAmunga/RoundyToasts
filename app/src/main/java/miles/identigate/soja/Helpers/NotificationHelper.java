package miles.identigate.soja.Helpers;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import miles.identigate.soja.Dashboard;
import miles.identigate.soja.R;

public class NotificationHelper {


    public static final String CHANNEL_ID = "Android Notifications";
    public static final String CHANNEL_NAME = "Android Amazing Notifications";
    public static final String CHANNEL_DESC = "Android Awesome Notifications";




    public static void displayNotification(Context context,String title,String body) {

        Intent intent=new Intent(context, Dashboard.class);

        PendingIntent pendingIntent=PendingIntent.getActivity(
                context,
                100,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT
        );
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true )
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(1, mBuilder.build());


    }

}
