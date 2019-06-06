package miles.identigate.soja.fragments;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.paperdb.Paper;
import miles.identigate.soja.R;
import miles.identigate.soja.UserInterface.RecordDriveIn;
import miles.identigate.soja.UserInterface.RecordResident;
import miles.identigate.soja.UserInterface.RecordWalkIn;
import miles.identigate.soja.UserInterface.ServiceProvider;
import miles.identigate.soja.activities.GuestList;
import miles.identigate.soja.activities.RegisterGuest;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewRegular;
import miles.identigate.soja.helpers.Preferences;

/**
 * Tpe of entry;manual or scanning
 * Fragment will be shown before all Record ins
 */
public class EntryTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String activity = "TargetActivity";
    @BindView(R.id.manualBtn)
    Button manualBtn;
    Unbinder unbinder;
    @BindView(R.id.visit_number)
    TextViewRegular visitNumber;

    // TODO: Rename and change types of parameters
    private int TargetActivity;
    OnEntrySelectedListener mListener;

    Integer driverPassengers = 0;

    Preferences preferences;

    public EntryTypeFragment() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static EntryTypeFragment newInstance(int param1) {
        EntryTypeFragment fragment = new EntryTypeFragment();
        Bundle args = new Bundle();
        args.putInt(activity, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TargetActivity = getArguments().getInt(activity);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_entry_type, container, false);
        unbinder = ButterKnife.bind(this, view);

        ImageView scan = view.findViewById(R.id.scan_icon);
        ImageView manual = view.findViewById(R.id.manual_icon);
        TextView record_type = view.findViewById(R.id.record_type);

        preferences = new Preferences(getActivity());


//        Track Driver Passengers
        driverPassengers = Paper.book().read(Common.PREF_CURRENT_DRIVER_PASS);

        if (driverPassengers != null && driverPassengers > 1) {
            visitNumber.setText(String.valueOf("Passenger " + driverPassengers));
            visitNumber.setVisibility(View.VISIBLE);
        } else {
            visitNumber.setVisibility(View.GONE);
        }


//        getActivity().getActionBar().setTitle("Record Resident");
        switch (TargetActivity) {
            case Common.DRIVE_IN:
                record_type.setText("RECORD DRIVE IN");
                break;
            case Common.WALK_IN:
                record_type.setText("RECORD WALK IN");
                break;
            case Common.SERVICE_PROVIDER:
                record_type.setText("RECORD SERVICE PROVIDER");
                break;
            case Common.RESIDENTS:
                record_type.setText("RECORD RESIDENT");
                break;
            case Common.INCIDENT:
                record_type.setText("RECORD INCIDENT");
                break;
            case Common.REGISTER_GUEST:
                record_type.setText("REGISTER NEW GUEST");
                break;
            case Common.ISSUE_TICKET:
                record_type.setText("ISSUE TICKET");
                break;
            case Common.CHECK_IN_GUEST:
                record_type.setText("CHECK IN");
                break;
        }
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnEntrySelected(Common.SCAN);
                //StartActivity(Common.SCAN);
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.OnEntrySelected(Common.MANUAL);
                //StartActivity(Common.MANUAL);
            }
        });

        if (preferences.getBaseURL().contains("events")) {
            manualBtn.setVisibility(View.VISIBLE);
        }else{

            manualBtn.setVisibility(View.GONE);
        }

        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();

                bundle.putBoolean("manual", true);
                switch (TargetActivity) {
                    case Common.DRIVE_IN:
                        startActivity(new Intent(getActivity(), RecordDriveIn.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;
                    case Common.WALK_IN:
                        startActivity(new Intent(getActivity(), RecordWalkIn.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;
                    case Common.SERVICE_PROVIDER:
                        startActivity(new Intent(getActivity(), ServiceProvider.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;
                    case Common.RESIDENTS:
                        startActivity(new Intent(getActivity(), RecordResident.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;
                    case Common.REGISTER_GUEST:
                        startActivity(new Intent(getActivity(), RegisterGuest.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;
                    case Common.ISSUE_TICKET:
                        startActivity(new Intent(getActivity(), GuestList.class).putExtras(bundle));
                        getActivity().overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
                        getActivity().finish();
                        break;

                }
            }
        });


        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface OnEntrySelectedListener {
        void OnEntrySelected(int type);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mListener = (OnEntrySelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }
}
