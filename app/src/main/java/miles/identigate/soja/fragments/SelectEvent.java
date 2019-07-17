package miles.identigate.soja.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import miles.identigate.soja.R;
import miles.identigate.soja.activities.CheckInGuest;
import miles.identigate.soja.adapters.EventAdapter;
import miles.identigate.soja.adapters.ServiceAdapter;
import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.interfaces.OnEventClick;
import miles.identigate.soja.models.Event;
import miles.identigate.soja.models.ServiceOption;
import miles.identigate.soja.models.TypeObject;
import miles.identigate.soja.services.DataService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SelectEvent extends Fragment implements OnEventClick {


    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    Unbinder unbinder;

    private static final String TAG = "SelectEvent";

    DatabaseHandler handler;

    EventAdapter eventAdapter;
    ArrayList<Event> events = new ArrayList<>();

    ArrayList<ServiceOption> serviceOptions = new ArrayList<>();
    DataService mService;
    Preferences preferences;


    public SelectEvent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_select_event, container, false);
        unbinder = ButterKnife.bind(this, view);

        preferences = new Preferences(getActivity());

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        handler = new DatabaseHandler(getActivity());
        mService = Common.getEventsDataService(getActivity());

        Log.d(TAG, "onCreateView: Start");

        loadEvents();


        return view;
    }

    private void loadEvents() {
        Log.d(TAG, "loadEvents: Start");

        events.clear();

        ArrayList<TypeObject> houses = handler.getTypes("houses", null);
        for (TypeObject house : houses) {
            Log.d(TAG, "loadEvents: Exists");
            events.add(new Event(house.getId(), house.getName()));
        }

        eventAdapter = new EventAdapter(getActivity(), events, this::onEventClick);

        recyclerView.setAdapter(eventAdapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onEventClick(Event event) {
//        Toast.makeText(getActivity(), "Selected " + event.getName(), Toast.LENGTH_SHORT).show();

        Bundle bundle = new Bundle();
        bundle.putString("eventID", event.getId());
        bundle.putString("eventName", event.getName());


        loadServiceOptions(event);

    }

    private void loadServiceOptions(Event event) {
        mService.getManagedServices(
                event.getId(),
                preferences.getCurrentUser().getPremiseZoneId()
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().toString());
                        String resultText = jsonObject.getString("result_text");
                        int resultCode = jsonObject.getInt("result_code");
                        JSONArray resultContent = jsonObject.optJSONArray("result_content");


//                        Log.d(TAG, "onResponse: Result Text"+resultText);
                        if (resultCode == 0 && resultText.equals("OK") && resultContent != null) {


                            Gson gson = new GsonBuilder().create();
                            serviceOptions = gson.fromJson(resultContent.toString(), new TypeToken<ArrayList<ServiceOption>>() {
                            }.getType());


                            if (serviceOptions.size() == 0) {
                                Bundle bundle = new Bundle();
                                bundle.putString("eventID", event.getId());
                                bundle.putString("eventName", event.getName());
                                bundle.putBoolean("direct", true);

                                ((CheckInGuest) getActivity()).displaySelectedScreen(R.id.nav_scan_event_ticket, bundle);
                            } else {
                                Bundle bundle = new Bundle();
                                bundle.putString("eventID", event.getId());
                                bundle.putString("eventName", event.getName());
                                bundle.putParcelableArrayList("services", serviceOptions);

                                ((CheckInGuest) getActivity()).displaySelectedScreen(R.id.nav_select_service, bundle);
                            }


                        } else {

                            Bundle bundle = new Bundle();
                            bundle.putString("eventID", event.getId());
                            bundle.putString("eventName", event.getName());
                            bundle.putBoolean("direct", true);


                            ((CheckInGuest) getActivity()).displaySelectedScreen(R.id.nav_scan_event_ticket, bundle);

                        }


                    } catch (JSONException e) {
//                        updateLayout("");
                        e.printStackTrace();
                    }

                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "onFailure: ", t);

            }
        });
    }

}
