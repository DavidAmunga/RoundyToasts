package miles.identigate.soja.search.guests;

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

public class SearchGuestsRepository {
    SearchGuestsDataSourceFactory searchGuestsDataSourceFactory;

    private static final String TAG = "SearchGuestsRepository";

    LiveData<PagedList<Guest>> ret_list;

    public SearchGuestsRepository(Context context) {
        searchGuestsDataSourceFactory = new SearchGuestsDataSourceFactory(context);
    }

    @MainThread
    public LiveData<PagedList<Guest>> getSearchResults(String query) {


        Log.d(TAG, "getSearchResults: " + query);

        searchGuestsDataSourceFactory.setSearchQuery(query);


        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setInitialLoadSizeHint(20)
                .setPageSize(20)
                .setPrefetchDistance(2)
                .build();


        try {
            Executor executor = Executors.newFixedThreadPool(Constants.NUMBERS_OF_THREADS);

            ret_list = new LivePagedListBuilder(searchGuestsDataSourceFactory, config)
                    .setInitialLoadKey(1)
                    .setFetchExecutor(executor)
                    .build();
        } catch (Exception e) {
            Log.i("retrofit", e.getMessage());

        }


        return ret_list;
    }
}
