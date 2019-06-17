package miles.identigate.soja.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import miles.identigate.soja.R;
import miles.identigate.soja.activities.CheckInGuest;
import miles.identigate.soja.adapters.ServiceAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.font.TextViewRegular;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.interfaces.OnServiceOptionClick;
import miles.identigate.soja.models.ServiceOption;
import miles.identigate.soja.services.DataService;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectServiceOption extends Fragment implements OnServiceOptionClick {


    private static final String TAG = "SelectServiceOption";
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;

    ServiceAdapter serviceAdapter;
    ArrayList<ServiceOption> serviceOptions = new ArrayList<>();
    DataService mService;
    @BindView(R.id.direct_scan_qr)
    TextViewRegular directScanQr;

    public SelectServiceOption() {
        // Required empty public constructor
    }

    String eventID, eventName;
    Preferences preferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_service_option, container, false);
        unbinder = ButterKnife.bind(this, view);

        mService = Common.getEventsDataService(getActivity());

        preferences = new Preferences(getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        Bundle bundle = getArguments();


        eventID = bundle.getString("eventID");
        eventName = bundle.getString("eventName");

        if (bundle.get("services") != null) {
            serviceOptions = bundle.getParcelableArrayList("services");

            serviceAdapter = new ServiceAdapter(getActivity(), serviceOptions, this::onServiceOptionClick);

            recyclerView.setAdapter(serviceAdapter);
        }

        directScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bundle.putBoolean("direct", true);

                ((CheckInGuest) getActivity()).displaySelectedScreen(R.id.nav_scan_event_ticket, bundle);
            }
        });


        return view;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onServiceOptionClick(ServiceOption serviceOption) {


        Bundle bundle = new Bundle();
        bundle.putString("eventID", eventID);
        bundle.putString("eventName", eventName);
        bundle.putString("serviceID", serviceOption.getServiceId());
        bundle.putString("serviceName", serviceOption.getDescription());

        ((CheckInGuest) getActivity()).displaySelectedScreen(R.id.nav_scan_event_ticket, bundle);


    }
}
