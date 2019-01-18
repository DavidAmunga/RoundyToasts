package miles.identigate.soja.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;

import miles.identigate.soja.Adapters.CheckInAdapter;
import miles.identigate.soja.FingerprintActivity;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.R;
import miles.identigate.soja.ScanActivity;
import miles.identigate.soja.ScanQRActivity;
import miles.identigate.soja.UserInterface.Incident;
import miles.identigate.soja.app.Common;

public class CheckIn extends Fragment {

    private String[] titles;
    private String[] descriptions;
    private Integer[] drawables;
    private Preferences preferences;
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

        ArrayList<String> checkinTitles = new ArrayList<>();
        checkinTitles.add("Drive In");
        checkinTitles.add("Walk In");
        checkinTitles.add("Residents");
        if (preferences.isFingerprintsEnabled())
            checkinTitles.add("Biometric Checkin");
        checkinTitles.add("Incident");


        ArrayList<String> checkinDescriptions =  new ArrayList<>();
        checkinDescriptions.add("Record driving visitor");
        checkinDescriptions.add("Record walking visitor");
        checkinDescriptions.add("Check in a resident");
        if (preferences.isFingerprintsEnabled())
            checkinDescriptions.add("Check in using biometrics");
        checkinDescriptions.add("Report an incident");

        ArrayList<Integer> checkinDrawables = new ArrayList<>();
        checkinDrawables.add(R.drawable.ic_action_car);
        checkinDrawables.add(R.drawable.ic_action_walk);
        checkinDrawables.add(R.drawable.ic_action_walk);
        if (preferences.isFingerprintsEnabled())
            checkinDrawables.add(R.drawable.fingerprint);
        checkinDrawables.add(R.drawable.ic_incident);



        Object[] a = checkinTitles.toArray();
        titles = Arrays.copyOf(a, a.length, String[].class);

        Object[] b = checkinDescriptions.toArray();
        descriptions = Arrays.copyOf(b, b.length, String[].class);

        Object[] c  = checkinDrawables.toArray();
        drawables = Arrays.copyOf(c, c.length, Integer[].class);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_check_in, container, false);
        ListView lv=(ListView)view.findViewById(R.id.options);


        CheckInAdapter checkInAdapter =new CheckInAdapter(getActivity(),titles,descriptions,drawables);
        lv.setAdapter(checkInAdapter);
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

                       /* extras.putInt("TargetActivity", Common.SERVICE_PROVIDER);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                        startActivity(new Intent(getActivity(), ScanQRActivity.class));
                        break;
                    case 3:
                        if (preferences.isFingerprintsEnabled()){
                            Intent fingerPrint = new Intent(getActivity(), FingerprintActivity.class);
                            fingerPrint.putExtra("CHECKOUT", false);
                            startActivity(fingerPrint);
                        }else {
                            startActivity(new Intent(getActivity(), Incident.class));
                        }
                        break;
                    case 4:

                       /* extras.putInt("TargetActivity", Common.RESIDENTS);
                        intent.putExtras(extras);
                        startActivity(intent);*/
                        startActivity(new Intent(getActivity(), Incident.class));
                        break;
                }
                getActivity().overridePendingTransition(R.anim.pull_in_left,R.anim.push_out_left);
            }
        });
        return view;
    }


}
