package miles.identigate.soja.adapters;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import miles.identigate.soja.R;
import miles.identigate.soja.interfaces.OnItemClick;
import miles.identigate.soja.models.DriveIn;
import miles.identigate.soja.models.Resident;
import miles.identigate.soja.models.ServiceProviderModel;

/**
 * Created by myles on 10/28/15.
 */
public class GuestAdapter {
    Activity activity;
    ArrayList<DriveIn> driveIns;
    ArrayList<DriveIn> walkIns;
    String type;
    ArrayList<ServiceProviderModel> serviceProviderModels;
    ArrayList<Resident> residents;

    OnItemClick mCallback;

    private static final String TAG = "DriveInAdapter";


    public GuestAdapter() {

    }


}
