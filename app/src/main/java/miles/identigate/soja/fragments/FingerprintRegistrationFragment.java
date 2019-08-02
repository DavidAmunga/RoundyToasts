package miles.identigate.soja.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import miles.identigate.soja.R;
import miles.identigate.soja.adapters.FingerprintAdapter;
import miles.identigate.soja.font.EditTextRegular;
import miles.identigate.soja.helpers.DatabaseHandler;
import miles.identigate.soja.helpers.NetworkHandler;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.listeners.OnRecyclerViewClicked;
import miles.identigate.soja.listeners.RecyclerTouchListener;
import miles.identigate.soja.models.PremiseResident;
import miles.identigate.soja.models.TypeObject;

public class FingerprintRegistrationFragment extends DialogFragment {
    ArrayList<PremiseResident> premiseResidents = new ArrayList<>();
    FingerprintAdapter fingerprintAdapter;
    private static final String TAG = "FingerprintRegistration";

    Context context;
    Preferences preferences;

    String premiseResidentResult;
    @BindView(R.id.searchbox)
    EditTextRegular searchbox;
    @BindView(R.id.nav_filter)
    ImageView navFilter;
    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.loading)
    ContentLoadingProgressBar loading;
    @BindView(R.id.content)
    LinearLayout content;
    Unbinder unbinder;
    @BindView(R.id.nav_refresh)
    ImageView navRefresh;


    private OnFragmentInteractionListener mListener;
    DatabaseHandler handler;

    ArrayList<String> hostTypes = new ArrayList<>();
    String filterItems[];

    int checkedItem = 0;

    public FingerprintRegistrationFragment() {
        // Required empty public constructor
    }

    public static FingerprintRegistrationFragment newInstance() {
        FingerprintRegistrationFragment fragment = new FingerprintRegistrationFragment();
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        preferences = new Preferences(context);
        handler = new DatabaseHandler(context);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fingerprint_registration, container, false);

        setHasOptionsMenu(true);
        unbinder = ButterKnife.bind(this, view);

//        recyclerView.setNestedScrollingEnabled(false);

        handler = new DatabaseHandler(getActivity());


        premiseResidents.clear();

        for (int i = 0; i < handler.getPremiseResidentsWithoutFingerprint().size(); i++) {
//            Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().get(i).getFirstName(), Toast.LENGTH_SHORT).show();
            premiseResidents.add(handler.getPremiseResidentsWithoutFingerprint().get(i));
        }


        fingerprintAdapter = new FingerprintAdapter(premiseResidents);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(fingerprintAdapter);

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new OnRecyclerViewClicked() {
            @Override
            public void onClick(View view, int position) {
                PremiseResident visitor = premiseResidents.get(position);
                new MaterialDialog.Builder(getActivity())
                        .title("Soja")
                        .content("Are you sure you want to register " + visitor.getFirstName() + " fingerprint")
                        .positiveText("Yes")
                        .negativeText("No")
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                dialog.dismiss();
                                onButtonPressed(visitor);

                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));


        searchbox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String text = searchbox.getText().toString().toLowerCase().trim();
                if (!text.isEmpty()) {
//                    Toast.makeText(context, "Not Empty", Toast.LENGTH_SHORT).show();
                    new SearchService().execute(text);
                } else {
//                    Toast.makeText(context, "Empty", Toast.LENGTH_SHORT).show();
                    for (int v = 0; v < handler.getPremiseResidentsWithoutFingerprint().size(); v++) {
//            Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().get(i).getFirstName(), Toast.LENGTH_SHORT).show();
                        premiseResidents.add(handler.getPremiseResidentsWithoutFingerprint().get(v));
                    }
                    fingerprintAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


//        TODO: Terrible Code Change ASAP


        for (PremiseResident premiseResident : premiseResidents) {
            if (premiseResident.getHostType() != null && !premiseResident.getHostType().equals("null")
            ) {
                if (!hostTypes.contains(premiseResident.getHostType())) {
                    hostTypes.add(premiseResident.getHostType());
                }
            }
        }


        filterItems = new String[hostTypes.size()];
        for (
                int i = 0; i < hostTypes.size(); i++) {

            filterItems[i] = hostTypes.get(i);
        }


        if (filterItems.length == 1) {
            navFilter.setVisibility(View.GONE);
        } else {
            showFilterDialog();
            navFilter.setVisibility(View.VISIBLE);
        }


        navFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterDialog();
            }
        });
        navRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new FetchResidents().execute();
            }
        });

        return view;
    }

    public void showFilterDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Filter By");


        builder.setSingleChoiceItems(filterItems, checkedItem, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                        Toast.makeText(Visitors.this, "Position: " + which + " Value: " + listItems[which], Toast.LENGTH_LONG).show();

//                        String click=listItems[which];
//                        Log.d(TAG, "onClick: "+click);
                String hostType = filterItems[which];

//                Toast.makeText(getActivity(), hostType, Toast.LENGTH_SHORT).show();

                filterHostTypes(hostType);

                checkedItem = which;

                dialog.dismiss();


            }
        });

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    public void onButtonPressed(PremiseResident visitor) {
        if (mListener != null) {
            mListener.onFragmentInteraction(visitor);
        }
        dismiss();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(PremiseResident visitor);
    }

    private class SearchService extends AsyncTask<String, Void, ArrayList<PremiseResident>> {
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<PremiseResident> doInBackground(String... strings) {
            String text = strings[0];
            ArrayList<PremiseResident> premiseResidents = new ArrayList<>();
//            Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().size(), Toast.LENGTH_SHORT).show();
            ArrayList<PremiseResident> allPremiseResidents = handler.getPremiseResidentsWithoutFingerprint();
            for (PremiseResident premiseResident : allPremiseResidents) {
                if (premiseResident.getFirstName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                    premiseResidents.add(premiseResident);
                } else if (premiseResident.getLastName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                    premiseResidents.add(premiseResident);
                } else if (premiseResident.getIdNumber().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                    premiseResidents.add(premiseResident);
                }
            }
            return premiseResidents;
        }

        @Override
        protected void onPostExecute(ArrayList<PremiseResident> _premiseResidents) {
            loading.setVisibility(View.GONE);
            premiseResidents.clear();

            for (int i = 0; i < _premiseResidents.size(); i++) {
                premiseResidents.add(_premiseResidents.get(i));
            }

            fingerprintAdapter.notifyDataSetChanged();

        }
    }

    private void filterHostTypes(String text) {

//        Toast.makeText(context, "Start", Toast.LENGTH_SHORT).show();
//        Toast.makeText(context, "Filter" + text, Toast.LENGTH_SHORT).show();

        ArrayList<PremiseResident> _premiseResidents = new ArrayList<>();

        ArrayList<PremiseResident> allPremiseResidents = handler.getPremiseResidentsWithoutFingerprint();
        for (PremiseResident premiseResident : allPremiseResidents) {
//            Log.d(TAG, "filterHostTypes: Get Host Type " + premiseResident.getHostType());
            if (premiseResident.getHostType() != null && premiseResident.getHostType().toLowerCase().trim().contains(text.toLowerCase().trim())) {

//                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                _premiseResidents.add(premiseResident);
            }
        }
//        Log.d(TAG, "getAllData: Premise Residents " + premiseResidents.size());


//        Toast.makeText(context, "Residents Size " + hostTypes.size(), Toast.LENGTH_SHORT).show();


        premiseResidents.clear();

        for (int i = 0; i < _premiseResidents.size(); i++) {
            premiseResidents.add(_premiseResidents.get(i));
        }


        fingerprintAdapter = new FingerprintAdapter(premiseResidents);

        fingerprintAdapter.notifyDataSetChanged();

        recyclerView.setAdapter(fingerprintAdapter);


    }


//    private class FilterService extends AsyncTask<String, Void, ArrayList<PremiseResident>> {
//        protected void onPreExecute() {
//            loading.setVisibility(View.VISIBLE);
//        }
//
//        @Override
//        protected ArrayList<PremiseResident> doInBackground(String... strings) {
//            String text = strings[0];
//
//
//            ArrayList<PremiseResident> premiseResidents = new ArrayList<>();
////            Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().size(), Toast.LENGTH_SHORT).show();
//            ArrayList<PremiseResident> allPremiseResidents = handler.getPremiseResidentsWithoutFingerprint();
//            for (PremiseResident premiseResident : allPremiseResidents) {
//                if (premiseResident.getHostType() != null && premiseResident.getHostType().toLowerCase().trim().contains(text.toLowerCase().trim())) {
//                    Toast.makeText(getActivity(), "Exists", Toast.LENGTH_SHORT).show();
//                    premiseResidents.add(premiseResident);
//                }
//            }
//            Toast.makeText(getActivity(), "Resident Size " + premiseResidents.size(), Toast.LENGTH_SHORT).show();
//            return premiseResidents;
//        }
//
//        @Override
//        protected void onPostExecute(ArrayList<PremiseResident> _premiseResidents) {
//            loading.setVisibility(View.GONE);
//            premiseResidents.clear();
//
//            for (int i = 0; i < _premiseResidents.size(); i++) {
//                premiseResidents.add(_premiseResidents.get(i));
//            }
//
//
//            fingerprintAdapter.notifyDataSetChanged();
//
//        }
//    }


    private class FetchResidents extends AsyncTask<Void, String, String> {
        MaterialDialog builder = new MaterialDialog.Builder(getActivity())
                .title("Soja")
                .titleGravity(GravityEnum.CENTER)
                .titleColor(getResources().getColor(R.color.ColorPrimary))
                .content("Fetching Residents")
                .progress(true, 0)
                .cancelable(true)
                .widgetColorRes(R.color.colorPrimary)
                .build();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            premiseResidentResult = NetworkHandler.GET(preferences.getBaseURL() + "houses-residents/?premise=" + preferences.getPremise());
            return "success";
        }

        @Override
        protected void onPostExecute(String s) {
            builder.dismiss();
            fetchResidentData(premiseResidentResult);
            super.onPostExecute(s);
        }

    }

    private void fetchResidentData(String premiseResidentResult) {
//        Toast.makeText(context, "Successful", Toast.LENGTH_SHORT).show();
        try {
            JSONObject premiseResidentObject = new JSONObject(premiseResidentResult);

//            Toast.makeText(context, "Before Context", Toast.LENGTH_SHORT).show();

            DatabaseHandler handler = new DatabaseHandler(getContext());

            SQLiteDatabase db = handler.getWritableDatabase();
            db.execSQL("DROP TABLE IF EXISTS " + DatabaseHandler.TABLE_PREMISE_RESIDENTS);

            db.execSQL(handler.CREATE_PREMISE_RESIDENTS_TABLE);

//            Toast.makeText(context, "After Create Residents Table", Toast.LENGTH_SHORT).show();


            if (premiseResidentObject.getInt("result_code") == 0 && premiseResidentObject.getString("result_text").equals("OK")) {
                JSONArray residentsArray = premiseResidentObject.getJSONArray("result_content");
                for (int i = 0; i < residentsArray.length(); i++) {
                    JSONObject resident = residentsArray.getJSONObject(i);
                    int length = 0;
                    if (resident.getString("length") != "null") {
                        length = Integer.valueOf(resident.getString("length"));
                    }
                    String fingerPrint = resident.get("fingerprint") == null ? null : resident.getString("fingerprint");
                    if (fingerPrint == "0")
                        fingerPrint = null;
                    fingerPrint = fingerPrint.replaceAll("\\n", "");
                    fingerPrint = fingerPrint.replace("\\r", "");
                    handler.insertPremiseResident(resident.getString("id"), resident.getString("id_number"), resident.getString("firstname"), resident.getString("lastname"), fingerPrint, length, resident.getString("house_id"), resident.getString("host_id"), resident.getString("host_type"));

//                    Toast.makeText(getActivity(), "Refresh Recycler View", Toast.LENGTH_SHORT).show();


                }

                premiseResidents.clear();

//                Toast.makeText(context, handler.getPremiseResidentsWithoutFingerprint().get(0).getFirstName(), Toast.LENGTH_SHORT).show();
                premiseResidents.addAll(handler.getPremiseResidentsWithoutFingerprint());

//                Log.d(TAG, "fetchResidentData: "+premiseResidents.size());
//
//                Toast.makeText(context, premiseResidents.size(), Toast.LENGTH_SHORT).show();
//
//
//
//                fingerprintAdapter = new FingerprintAdapter(premiseResidents);
//                recyclerView.setAdapter(fingerprintAdapter);
                fingerprintAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(getActivity(), "Couldn't retrieve premise residents", Toast.LENGTH_SHORT).show();
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}