package miles.identigate.soja.guests;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.content.Context;
import android.support.annotation.MainThread;
import android.util.Log;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.utility.AppsExecutor;

public class GuestsRepository {
    GuestsDataSourceFactory guestsDataSourceFactory;

    private static final String TAG = "GuestsRepository";

    LiveData<PagedList<Guest>> ret_list;

    public GuestsRepository(Context context) {
        guestsDataSourceFactory = new GuestsDataSourceFactory(context);
    }


    @MainThread
    public LiveData<PagedList<Guest>> getGuests() {

        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(2)
                .build();


        try {
            Executor executor = Executors.newFixedThreadPool(Constants.NUMBERS_OF_THREADS);

            ret_list = new LivePagedListBuilder(guestsDataSourceFactory, config)
                    .setInitialLoadKey(1)
                    .setFetchExecutor(executor)
                    .build();
        } catch (Exception e) {
            Log.i("retrofit", e.getMessage());

        }

        Log.d(TAG, "getGuests: "+ret_list.toString());

        return ret_list;

    }


}
