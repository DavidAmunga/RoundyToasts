package miles.identigate.soja.service;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;
import android.arch.paging.PagedList;
import android.content.Context;
import android.util.Log;

import miles.identigate.soja.service.network.GuestNetwork;
import miles.identigate.soja.service.network.paging.guests.NetGuestDataSourceFactory;
import miles.identigate.soja.service.storage.GuestDB;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;
import rx.schedulers.Schedulers;

public class GuestsRepository {
    private static final String TAG = "GuestsRepository";


    private static GuestsRepository instance;
    final private GuestNetwork network;
    final private GuestDB database;
    final private MediatorLiveData liveDataMerger;
    NetGuestDataSourceFactory dataSourceFactory;


    public GuestsRepository(Context context) {
        dataSourceFactory = new NetGuestDataSourceFactory(context);

        network = new GuestNetwork(dataSourceFactory, boundaryCallBack);
        database = GuestDB.getInstance(context.getApplicationContext());


//                When we get new Appointments from network we set them into the database
        liveDataMerger = new MediatorLiveData();
        liveDataMerger.addSource(network.getPagedGuests(), value -> {
            liveDataMerger.setValue(value);

            Log.d(TAG, "GuestsRepository: " + value.toString());
        });


//        Save Appointments into DB

        dataSourceFactory.getGuests().
                observeOn(Schedulers.io()).
                subscribe(appointment -> {
                    ((GuestDB) database).guestDao().insertGuest(appointment);
                });


    }

    private PagedList.BoundaryCallback<Guest> boundaryCallBack = new PagedList.BoundaryCallback<Guest>() {
        @Override
        public void onZeroItemsLoaded() {
            super.onZeroItemsLoaded();

            liveDataMerger.addSource(database.getGuests(), value -> {
                liveDataMerger.setValue(value);
                liveDataMerger.removeSource(database.getGuests());
            });

        }
    };

    public static GuestsRepository getInstance(Context context) {
        if (instance == null) {
            instance = new GuestsRepository(context);
        }
        return instance;
    }

    public LiveData<NetworkState> getNetworkState() {
        return network.getNetworkState();
    }

    public LiveData<PagedList<Guest>> getGuests() {
        return liveDataMerger;
    }

    public void refresh() {

        dataSourceFactory.refresh();

    }


}
