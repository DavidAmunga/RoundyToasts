package miles.identigate.soja.service.network.paging.guests;

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

public class NetGuestPageKeyedDataSource extends PageKeyedDataSource<String, Guest> {

    private static final String TAG = "NetGuestPageKeyedDataSo";

    private final APIInterface apiService;

    Preferences preferences;

    private final MutableLiveData networkState;
    private final ReplaySubject<Guest> guestObservable;

    //    Size of page that we want
    public static final int PAGE_SIZE = 25;

    private static final int FIRST_PAGE = 0;


    public NetGuestPageKeyedDataSource(Context context) {
        preferences = new Preferences(context);
        apiService = APIClient.getClient(preferences, Common.GUEST_LIST);
        networkState = new MutableLiveData();
        guestObservable = ReplaySubject.create();
    }

    public MutableLiveData getNetworkState() {
        return networkState;
    }

    public ReplaySubject<Guest> getGuests() {
        return guestObservable;
    }


    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Guest> callback) {
        Log.d(TAG, "loadInitial: " + params.requestedLoadSize);

        networkState.postValue(NetworkState.LOADING);

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
                    callback.onResult(response.body(), null, Integer.toString(FIRST_PAGE + 1));
                    networkState.postValue(NetworkState.LOADED);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        response.body().forEach(guestObservable::onNext);
                    } else {
                        Log.d(TAG, "onResponse: " + response.message());
                        networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    }
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
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));

            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Guest> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Guest> callback) {
        Log.d(TAG, "loadAfter: " + params.key);

        networkState.postValue(NetworkState.LOADING);

        final AtomicInteger page = new AtomicInteger(0);
        try {
            page.set(Integer.parseInt(params.key));
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
                    callback.onResult(response.body(), Integer.toString(page.get() + 1));
                    networkState.postValue(NetworkState.LOADED);


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        response.body().forEach(guestObservable::onNext);
                    } else {
                        Log.d(TAG, "onResponse: " + response.message());
                        networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    }
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
                networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));

            }
        });

    }
}
