package miles.identigate.soja.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import miles.identigate.soja.Adapters.CheckInAdapter;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.PremiseResident;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.Services.IFCMService;
import miles.identigate.soja.SmsCheckInActivity;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.app.Common;

public class CheckIn extends Fragment {



    private static final String TAG = "CheckIn";
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    Unbinder unbinder;

    private String[] titles;
    private String[] descriptions;
    private Integer[] drawables;
    private Preferences preferences;
    ArrayList<String> checkinTitles = new ArrayList<>();
    ArrayList<PremiseResident> premiseResidents = new ArrayList<>();

    String premiseResidentResult;
    DatabaseHandler handler;




    public static CheckIn newInstance(String param1, String param2) {
        CheckIn fragment = new CheckIn();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public CheckIn() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = new Preferences(getActivity());


//        CASUALS
        if (!preferences.getBaseURL().contains("casuals")) {
            checkinTitles.add("Drive In");
        }


        checkinTitles.add("Walk In");

        if (!preferences.getBaseURL().contains("casuals")) {
            checkinTitles.add("Residents");
        }

        if (preferences.isFingerprintsEnabled())
            checkinTitles.add("Biometric Checkin");


        if (preferences.isSMSCheckInEnabled())
            checkinTitles.add("SMS Checkin");
//        checkinTitles.add("Incident");


        ArrayList<String> checkinDescriptions = new ArrayList<>();

        if (!preferences.getBaseURL().contains("casuals")) {
            checkinDescriptions.add("Record driving visitor");
        }

        if (preferences.getBaseURL().contains("casuals")) {
            checkinDescriptions.add("Record walking employee");
        } else {
            checkinDescriptions.add("Record walking visitor");
        }

        if (!preferences.getBaseURL().contains("casuals")) {
            checkinDescriptions.add("Check in a resident");
        }

        if (preferences.isFingerprintsEnabled())
            checkinDescriptions.add("Check in using biometrics");


        if (preferences.isSMSCheckInEnabled())
            checkinDescriptions.add("Check in a Visitor without an ID");
//        checkinDescriptions.add("Report an incident");

        ArrayList<Integer> checkinDrawables = new ArrayList<>();

        if (!preferences.getBaseURL().contains("casuals")) {
            checkinDrawables.add(R.drawable.ic_drive_in_new);
        }


        checkinDrawables.add(R.drawable.ic_walk_in_new);

        if (!preferences.getBaseURL().contains("casuals")) {
            checkinDrawables.add(R.drawable.ic_resident_icon_new);
        }

        if (preferences.isFingerprintsEnabled())
            checkinDrawables.add(R.drawable.fingerprint);
        if (preferences.isSMSCheckInEnabled())
            checkinDrawables.add(R.drawable.ic_sms_check_in_new);
//        checkinDrawables.add(R.drawable.ic_siren);


        Object[] a = checkinTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkinDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = checkinDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_in, container, false);

        unbinder = ButterKnife.bind(this, view);


        ListView lv = view.findViewById(R.id.options);

        setListViewHeightBasedOnItems(lv);

//        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
////        int pxWidth = displayMetrics.widthPixels;
////        float dpWidth = pxWidth / displayMetrics.density;
//        int pxHeight = displayMetrics.heightPixels;
//        float dpHeight = pxHeight / displayMetrics.density;
//
//        lv.setMinimumHeight(pxHeight);

        CheckInAdapter checkInAdapter = new CheckInAdapter(getActivity(), titles, descriptions, drawables, "checkin");
        lv.setAdapter(checkInAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ScanActivity.class);
                Bundle extras = new Bundle();

                String item = parent.getItemAtPosition(position).toString();


                Log.d(TAG, "onItemClick: " + parent.getItemAtPosition(position).toString());
                switch (item) {
                    case "Drive In":
                        extras.putInt("TargetActivity", Common.DRIVE_IN);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case "Walk In":
                        extras.putInt("TargetActivity", Common.WALK_IN);
                        intent.putExtras(extras);

                        startActivity(intent);
                        break;
                    case "Residents":

                       /* extras.putInt("TargetActivity", Common.SERVICE_PROVIDER);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                        startActivity(new Intent(getActivity(), ScanQRActivity.class));
                        break;
                    case "Biometric Checkin":
                        if (preferences.isFingerprintsEnabled()) {
//                            Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
//                            fingerPrint.putExtra("CHECKOUT", false);
//                            startActivity(fingerPrint);
                        } else {
                            startActivity(new Intent(getActivity(), Incident.class));
                        }
                        break;
                    case "SMS Checkin":
                        if (preferences.isSMSCheckInEnabled()) {
                       /* extras.putInt("TargetActivity", Common.RESIDENTS);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                            startActivity(new Intent(getActivity(), SmsCheckInActivity.class));

                        } else {
                            Toast.makeText(getActivity(), "SMS Not Enabled", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getActivity(), SmsCheckInActivity.class));
                        }
                        break;
                    case "Incident":

                       /* extras.putInt("TargetActivity", Common.RESIDENTS);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                        startActivity(new Intent(getActivity(), Incident.class));
                        break;
                }
                getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_left);
            }
        });


        return view;
    }


    public static boolean setListViewHeightBasedOnItems(ListView listView) {

        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter != null) {

            int numberOfItems = listAdapter.getCount();

            // Get total height of all items.
            int totalItemsHeight = 0;
            for (int itemPos = 0; itemPos < numberOfItems; itemPos++) {
                View item = listAdapter.getView(itemPos, null, listView);
                item.measure(0, 0);
                totalItemsHeight += item.getMeasuredHeight();
            }

            // Get total height of all item dividers.
            int totalDividersHeight = listView.getDividerHeight() *
                    (numberOfItems - 1);

            // Set list height.
            ViewGroup.LayoutParams params = listView.getLayoutParams();
            params.height = totalItemsHeight + totalDividersHeight;
            listView.setLayoutParams(params);
            listView.requestLayout();

            return true;

        } else {
            return false;
        }}

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}


