package miles.identigate.soja.service.network;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import miles.identigate.soja.helpers.Constants;
import miles.identigate.soja.service.network.paging.guests.NetGuestDataSourceFactory;
import miles.identigate.soja.service.network.paging.guests.NetGuestPageKeyedDataSource;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;

import static miles.identigate.soja.helpers.Constants.LOADING_PAGE_SIZE;

public class GuestNetwork {
    private static final String TAG = "GuestNetwork";

    final private LiveData<PagedList<Guest>> guestsPaged;
    final private LiveData<NetworkState> networkState;

    public GuestNetwork(NetGuestDataSourceFactory dataSourceFactory, PagedList.BoundaryCallback<Guest> boundaryCallback) {
        PagedList.Config pagedListConfig = (new PagedList.Config.Builder()).setEnablePlaceholders(false)
                .setInitialLoadSizeHint(LOADING_PAGE_SIZE).setPageSize(LOADING_PAGE_SIZE).build();

        networkState = Transformations.switchMap(dataSourceFactory.getNetworkStatus(),
                (Function<NetGuestPageKeyedDataSource, LiveData<NetworkState>>)
                        NetGuestPageKeyedDataSource::getNetworkState
        );
        Executor executor = Executors.newFixedThreadPool(Constants.NUMBERS_OF_THREADS);
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder(dataSourceFactory, pagedListConfig);


        guestsPaged = livePagedListBuilder.
                setFetchExecutor(executor).
                setBoundaryCallback(boundaryCallback).
                build();



    }

    public LiveData<PagedList<Guest>> getPagedGuests() {
        return guestsPaged;
    }

    public LiveData<NetworkState> getNetworkState() {
        return networkState;
    }
}
