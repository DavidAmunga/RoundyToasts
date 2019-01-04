package miles.identigate.soja.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;

import java.util.ArrayList;

import miles.identigate.soja.Adapters.FingerprintAdapter;
import miles.identigate.soja.Helpers.DatabaseHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.PremiseResident;
import miles.identigate.soja.R;
import miles.identigate.soja.listeners.OnRecyclerViewClicked;
import miles.identigate.soja.listeners.RecyclerTouchListener;

public class FingerprintRegistrationFragment extends DialogFragment {
    EditText searchbox;
    ContentLoadingProgressBar loading;
    RecyclerView recyclerView;
    ArrayList<PremiseResident> premiseResidents = new ArrayList<>();
    LinearLayoutManager lLayout;
    NestedScrollView main_content;
    FingerprintAdapter fingerprintAdapter;

    Context context;
    Preferences preferences;

    private OnFragmentInteractionListener mListener;
    DatabaseHandler handler;

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
        searchbox = (EditText)view.findViewById(R.id.searchbox);
        loading = (ContentLoadingProgressBar)view.findViewById(R.id.loading);
        recyclerView = (RecyclerView)view.findViewById(R.id.recyclerView);
        main_content = (NestedScrollView)view.findViewById(R.id.main_content);

        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setNestedScrollingEnabled(false);

        fingerprintAdapter = new FingerprintAdapter(premiseResidents);

        lLayout = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(lLayout);
        recyclerView.setAdapter(fingerprintAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new OnRecyclerViewClicked() {
            @Override
            public void onClick(View view, int position) {
                PremiseResident visitor = premiseResidents.get(position);
                onButtonPressed(visitor);
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
                if (!text.isEmpty()){
                    new SearchService().execute(text);
                }else{
                    premiseResidents.clear();
                    fingerprintAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
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


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(PremiseResident visitor);
    }

    private class SearchService extends AsyncTask<String, Void,ArrayList<PremiseResident>> {
        protected void onPreExecute(){
            main_content.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<PremiseResident> doInBackground(String... strings) {
            String text =  strings[0];
            ArrayList<PremiseResident> premiseResidents = new ArrayList<>();
            ArrayList<PremiseResident> allPremiseResidents = handler.getPremiseResidents();
            for (PremiseResident premiseResident: allPremiseResidents){
                if (premiseResident.getFirstName().toLowerCase().trim().contains(text.toLowerCase().trim())) {
                    premiseResidents.add(premiseResident);
                }else if (premiseResident.getLastName().toLowerCase().trim().contains(text.toLowerCase().trim())){
                    premiseResidents.add(premiseResident);
                }else if (premiseResident.getIdNumber().toLowerCase().trim().contains(text.toLowerCase().trim())){
                    premiseResidents.add(premiseResident);
                }
            }
            return premiseResidents;
        }
        @Override
        protected void onPostExecute(ArrayList<PremiseResident> _premiseResidents){
            loading.setVisibility(View.GONE);
            main_content.setVisibility(View.VISIBLE);

            premiseResidents.clear();

            for (int i = 0; i < _premiseResidents.size(); i++) {
                premiseResidents.add(_premiseResidents.get(i));
            }

            fingerprintAdapter.notifyDataSetChanged();

        }
    }
}
