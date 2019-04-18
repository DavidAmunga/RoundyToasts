package miles.identigate.soja.guests;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Preferences;
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

    private final MutableLiveData networkState;

    //    Size of page that we want
    public static final int PAGE_SIZE = 25;

    private static final int FIRST_PAGE = 0;


    public GuestsDataSource(Context context) {
        preferences = new Preferences(context);
        apiService = APIClient.getClient(preferences, Common.GUEST_LIST);
        networkState = new MutableLiveData();

    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<Integer> params, @NonNull final LoadInitialCallback<Integer, Guest> callback) {
//        Log.d(TAG, "loadInitial: " + params.requestedLoadSize);


        final Call<ArrayList<Guest>> callBack =
                apiService.getGuests(
                        preferences.getCurrentUser().getPremiseId(),
                        FIRST_PAGE,
                        PAGE_SIZE,
                        null
                );

        callBack.enqueue(new Callback<ArrayList<Guest>>() {
            @Override
            public void onResponse(Call<ArrayList<Guest>> call, Response<ArrayList<Guest>> response) {
                if (response.isSuccessful()) {
                    callback.onResult(response.body(), null, FIRST_PAGE + 1);

                }
            }

            @Override
            public void onFailure(Call<ArrayList<Guest>> call, Throwable t) {

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


        final AtomicInteger page = new AtomicInteger(0);
        try {
            page.set(params.key);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        final Call<ArrayList<Guest>> callBack =
                apiService.getGuests(
                        preferences.getCurrentUser().getPremiseId(),
                        page.get(),
                        PAGE_SIZE,
                        null
                );

        callBack.enqueue(new Callback<ArrayList<Guest>>() {
            @Override
            public void onResponse(Call<ArrayList<Guest>> call, Response<ArrayList<Guest>> response) {
                if (response.isSuccessful()) {
                    callback.onResult(response.body(), page.get() + 1);

                }
            }

            @Override
            public void onFailure(Call<ArrayList<Guest>> call, Throwable t) {

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