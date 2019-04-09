package miles.identigate.soja.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import miles.identigate.soja.R;
import miles.identigate.soja.app.Common;

/**
 * Tpe of entry;manual or scanning
 * Fragment will be shown before all Record ins
 */
public class EntryTypeFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String activity = "TargetActivity";

    // TODO: Rename and change types of parameters
    private int TargetActivity;
    OnEntrySelectedListener mListener;

    public EntryTypeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @return A new instance of fragment EntryTypeFragment.
     */
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
        ImageView scan = view.findViewById(R.id.scan_icon);
        ImageView manual = view.findViewById(R.id.manual_icon);
        TextView record_type = view.findViewById(R.id.record_type);

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
        return view;
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