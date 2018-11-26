package miles.identigate.soja.Fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

import miles.identigate.soja.Adapters.FingerprintAdapter;
import miles.identigate.soja.Helpers.NetworkHandler;
import miles.identigate.soja.Helpers.Preferences;
import miles.identigate.soja.Models.Visitor;
import miles.identigate.soja.R;
import miles.identigate.soja.listeners.OnRecyclerViewClicked;
import miles.identigate.soja.listeners.RecyclerTouchListener;

public class FingerprintRegistrationFragment extends DialogFragment {
    EditText searchbox;
    ContentLoadingProgressBar loading;
    RecyclerView recyclerView;
    ArrayList<Visitor> visitors = new ArrayList<>();
    LinearLayoutManager lLayout;
    NestedScrollView main_content;
    FingerprintAdapter fingerprintAdapter;

    Context context;
    Preferences preferences;

    private OnFragmentInteractionListener mListener;

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

        fingerprintAdapter = new FingerprintAdapter(visitors);

        lLayout = new LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);

        recyclerView.setLayoutManager(lLayout);
        recyclerView.setAdapter(fingerprintAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(context, recyclerView, new OnRecyclerViewClicked() {
            @Override
            public void onClick(View view, int position) {
                Visitor visitor = visitors.get(position);
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
                    String s = preferences.getBaseURL();
                    String url = s.substring(0,s.length()-11);
                    new SearchService().execute((url+"api/visitors/visitors_in/" + preferences.getPremise()));
                }else{
                    visitors.clear();
                    fingerprintAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return view;
    }

    public void onButtonPressed(Visitor visitor) {
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
        void onFragmentInteraction(Visitor visitor);
    }

    private class SearchService extends AsyncTask<String, Void,String> {
        protected void onPreExecute(){
            main_content.setVisibility(View.GONE);
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            return NetworkHandler.GET(strings[0]);
        }
        @Override
        protected void onPostExecute(String s){
            loading.setVisibility(View.GONE);
            main_content.setVisibility(View.VISIBLE);
            if(s !=null) {
                Object json = null;
                try {
                    json = new JSONTokener(s).nextValue();
                    if (json instanceof JSONObject) {
                        JSONObject object = new JSONObject(s);
                        JSONArray array = object.getJSONArray("result_content");
                        if (array.length() > 0) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject item = array.getJSONObject(i);
                                String name = item.getString("fullname");
                                String id = item.getString("id_number");
                                String entry = item.getString("entry_time");
                                String house = item.getString("house");

                                Visitor visitor = new Visitor();
                                visitor.setName(name);
                                visitor.setNational_id(id);
                                visitors.add(visitor);
                            }
                            loading.setVisibility(View.GONE);
                            main_content.setVisibility(View.VISIBLE);
                            fingerprintAdapter.notifyDataSetChanged();
                        } else {
                            loading.setVisibility(View.GONE);
                            main_content.setVisibility(View.GONE);
                        }
                    } else {
                        loading.setVisibility(View.GONE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}
