package miles.identigate.soja.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.regula.sdk.CaptureActivity;
import com.regula.sdk.DocumentReader;

import java.io.IOException;
import java.io.InputStream;

import miles.identigate.soja.Adapters.Option;
import miles.identigate.soja.EntryTypeActivity;
import miles.identigate.soja.Helpers.Constants;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.app.Common;

public class CheckIn extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String[] titles={
            "Drive In",
            "Walk In",
            "Service Provider",
            "Residents",
            "Incident"
    };
    private String[] descriptions={
            "Record driving visitor",
            "Record walking visitor",
            "Record a service provider",
            "Check in a resident",
            "Report an incident"

    };
    private int[] drawables={
            R.drawable.ic_action_car,
            R.drawable.ic_action_walk,
            R.drawable.ic_action_many,
            R.drawable.ic_action_walk,
            R.drawable.ic_incident

    };
    public static CheckIn newInstance(String param1, String param2) {
        CheckIn fragment = new CheckIn();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public CheckIn() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_check_in, container, false);
        ListView lv=(ListView)view.findViewById(R.id.options);
        Option option=new Option(getActivity(),titles,descriptions,drawables);
        lv.setAdapter(option);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getActivity(), ScanActivity.class);
                Bundle extras=new Bundle();
             switch (position) {
                    case 0:
                        extras.putInt("TargetActivity", Common.DRIVE_IN);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case 1:
                        extras.putInt("TargetActivity", Common.WALK_IN);
                        intent.putExtras(extras);
                        startActivity(intent);
                         break;
                    case 2:

                        extras.putInt("TargetActivity", Common.SERVICE_PROVIDER);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case 3:

                        extras.putInt("TargetActivity", Common.RESIDENTS);
                        intent.putExtras(extras);
                        startActivity(intent);
                        break;
                    case 4:
                        /*extras.putInt("TargetActivity", Common.INCIDENT);
                        intent.putExtras(extras);*/
                        startActivity(new Intent(getActivity(), Incident.class));
                        break;
                }
                //startActivity(intent);
                getActivity().overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_left);
            }
        });
        return view;
    }
    public Bundle dummyData(){
        Bundle data =new Bundle();
        data.putString(Common.DOB,new Constants().getCurrentTimeStamp());
        data.putString(Common.SEX,"M");
        data.putString(Common.FIRST_NAME,"Miles");
        data.putString(Common.OTHER_NAMES,"Obare");
        data.putString(Common.ID_TYPE,"NID");
        data.putString(Common.ID_NUMBER,"33166582");
        return data;

    }


}
