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
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.SmsCheckInActivity;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.activities.CheckInGuest;
import miles.identigate.soja.activities.GuestList;
import miles.identigate.soja.activities.InviteesList;
import miles.identigate.soja.adapters.CheckInAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.PremiseResident;

/**
 * A simple {@link Fragment} subclass.
 */
public class Invitees extends Fragment {

    private static final String TAG = "Invitees";

    private String[] titles;
    private String[] descriptions;
    private Integer[] drawables;
    private Preferences preferences;
    ArrayList<String> inviteeTitles = new ArrayList<>();
    ArrayList<String> inviteeDescriptions = new ArrayList<>();
    ArrayList<Integer> inviteeDrawables = new ArrayList<>();


    @BindView(R.id.options)
    ListView options;
    @BindView(R.id.linearLayout)
    LinearLayout linearLayout;
    Unbinder unbinder;

    public Invitees() {
        // Required empty public constructor
    }

    public static Invitees newInstance(String param1, String param2) {
        Invitees fragment = new Invitees();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_invitees, container, false);
        unbinder = ButterKnife.bind(this, view);


        inviteeDrawables.clear();
        inviteeDescriptions.clear();
        inviteeTitles.clear();

        preferences = new Preferences(getActivity());

        inviteeDrawables.add(R.drawable.ic_drive_in_log_new);
        inviteeDrawables.add(R.drawable.ic_walk_in_log_new);

        inviteeTitles.add("Drive In");
        inviteeTitles.add("Walk In");

        inviteeDescriptions.add("Check in a Driving Invited Guest");
        inviteeDescriptions.add("Check in a Walking Invited Guest");

        Object[] a = inviteeTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = inviteeDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c = inviteeDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);


        setListViewHeightBasedOnItems(options);

        CheckInAdapter checkInAdapter = new CheckInAdapter(getActivity(), titles, descriptions, drawables, "checkin");
        options.setAdapter(checkInAdapter);
        options.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), InviteesList.class);
                Bundle extras = new Bundle();

                String item = parent.getItemAtPosition(position).toString();


                Log.d(TAG, "onItemClick: " + parent.getItemAtPosition(position).toString());
                switch (item) {
                    case "Drive In":
                        extras.putInt("TargetAction", Common.DRIVE_IN_INVITEE);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case "Walk In":
                        extras.putInt("TargetAction", Common.WALK_IN_INVITEE);
                        intent.putExtras(extras);
                        startActivity(intent);
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
