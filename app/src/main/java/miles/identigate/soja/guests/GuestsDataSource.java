package miles.identigate.soja.guests;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Preferences;
import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.service.network.api.APIClient;
import miles.identigate.soja.service.network.api.APIInterface;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rx.subjects.ReplaySubject;

public class GuestsDataSource extends PageKeyedDataSource<Integer, Guest> {


    private static final String TAG = "GuestsDataSource";

    private final APIInterface apiService;

    Preferences preferences;


    //    Size of page that we want
    public static final int PAGE_SIZE = 25;

    private static final int FIRST_PAGE = 0;


    public GuestsDataSource(Context context) {
        preferences = new Preferences(context);
        apiService = APIClient.getClient(preferences, Common.GUEST_LIST);

    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Guest> callback) {
//        Log.d(TAG, "loadInitial: " + params.requestedLoadSize);


        final Call<QueryResponse> callBack =
                apiService.getGuests(
                        preferences.getCurrentUser().getPremiseId(),
                        FIRST_PAGE,
                        PAGE_SIZE,
                        null
                );

        callBack.enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    QueryResponse queryResponse = response.body();
                    if (queryResponse.getResultText().equals("OK") && queryResponse.getResultCode() == 0 && queryResponse.getResultContent() != null) {

                        Gson gson = new Gson();
                        JsonArray resultContent = gson.toJsonTree(queryResponse.getResultContent()).getAsJsonArray();
                        Type guestType = new TypeToken<ArrayList<Guest>>() {
                        }.getType();


                        ArrayList<Guest> guests = gson.fromJson(resultContent, guestType);


                        callback.onResult(guests, null, FIRST_PAGE + 1);

                    }

                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: ", t);

                String errorMessage;

                if (t.getMessage() == null) {
                    errorMessage = "unknown error";
                } else {
                    errorMessage = t.getMessage();
                }

            }
        });
    }


    @Override
    public void loadAfter(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Guest> callback) {
        Log.d(TAG, "loadAfter: " + params.key);


        final Call<QueryResponse> callBack =
                apiService.getGuests(
                        preferences.getCurrentUser().getPremiseId(),
                        params.key,
                        PAGE_SIZE,
                        null
                );

        callBack.enqueue(new Callback<QueryResponse>() {
            @Override
            public void onResponse(Call<QueryResponse> call, Response<QueryResponse> response) {
                if (response.isSuccessful()) {
                    QueryResponse queryResponse = response.body();

                    Log.d(TAG, "onResponse: " + response.body());

                    if (queryResponse.getResultText().equals("OK") && queryResponse.getResultCode() == 0 && queryResponse.getResultContent() != null) {

                        Gson gson = new Gson();
                        JsonArray resultContent = gson.toJsonTree(queryResponse.getResultContent()).getAsJsonArray();
                        Type guestType = new TypeToken<ArrayList<Guest>>() {
                        }.getType();


                        ArrayList<Guest> guests = gson.fromJson(resultContent, guestType);

                        JsonObject resultDetail = gson.toJsonTree(queryResponse.getResultDetail()).getAsJsonObject();



                        boolean hasMore = resultDetail.get("hasMore").getAsBoolean();


                        Log.d(TAG, "onResponse: " + hasMore);


                        Integer key = hasMore ? params.key + 1 : null;
                        callback.onResult(guests, key);

                    }

                }
            }

            @Override
            public void onFailure(Call<QueryResponse> call, Throwable t) {

                Log.d(TAG, "onFailure: ", t);

                String errorMessage;

                if (t.getMessage() == null) {
                    errorMessage = "unknown error";
                } else {
                    errorMessage = t.getMessage();
                }

            }
        });

    }


    @Override
    public void loadBefore(@NonNull LoadParams<Integer> params, @NonNull LoadCallback<Integer, Guest> callback) {

    }


}