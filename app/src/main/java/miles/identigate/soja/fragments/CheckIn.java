package miles.identigate.soja.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import miles.identigate.soja.FingerprintActivity;
import miles.identigate.soja.activities.CheckInGuest;
import miles.identigate.soja.activities.GuestList;
import miles.identigate.soja.activities.ScanTicket;
import miles.identigate.soja.adapters.CheckInAdapter;
import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.PremiseResident;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.ScanQRActivity;
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
    ArrayList<String> checkInDescriptions = new ArrayList<>();
    ArrayList<Integer> checkInDrawables = new ArrayList<>();
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


        Constants.setDashboardCheckIn(preferences, checkinTitles, checkInDrawables, checkInDescriptions);


        Object[] a = checkinTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkInDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = checkInDrawables.toArray();
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
                    case "Register":
                        extras.putInt("TargetActivity", Common.REGISTER_GUEST);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case "Issue Ticket":
//                        extras.putInt("TargetActivity", Common.ISSUE_TICKET);
//                        intent.putExtras(m);

                        startActivity(new Intent(getActivity(), GuestList.class));
                        break;
                    case "Check In":
                        startActivity(new Intent(getActivity(), CheckInGuest.class));
                        break;
                    case "Tickets":
                        startActivity(new Intent(getActivity(), ScanTicket.class));
                        break;
                    case "Residents":

                       /* extras.putInt("TargetActivity", Common.SERVICE_PROVIDER);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                        startActivity(new Intent(getActivity(), ScanQRActivity.class));
                        break;
                    case "Biometric Checkin":
                        if (preferences.isFingerprintsEnabled()) {
                            Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
                            fingerPrint.putExtra("CHECKOUT", false);
                            startActivity(fingerPrint);
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
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}


