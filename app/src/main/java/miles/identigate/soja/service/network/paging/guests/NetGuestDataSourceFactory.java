package miles.identigate.soja.service.network.paging.guests;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.content.Context;

import miles.identigate.soja.service.storage.model.Guest;
import rx.subjects.ReplaySubject;

public class NetGuestDataSourceFactory extends DataSource.Factory {
    private static final String TAG = "NetGuestDataSourceFacto";

    private MutableLiveData<NetGuestPageKeyedDataSource> networkStatus;
    private NetGuestPageKeyedDataSource guestPageKeyedDataSource;

    public NetGuestDataSourceFactory(Context context) {
        this.networkStatus = new MutableLiveData<>();
        guestPageKeyedDataSource=new NetGuestPageKeyedDataSource(context);
    }


    @Override
    public DataSource create() {
        networkStatus.postValue(guestPageKeyedDataSource);
        return guestPageKeyedDataSource;
    }

    public MutableLiveData<NetGuestPageKeyedDataSource> getNetworkStatus() {
        return networkStatus;
    }

    public ReplaySubject<Guest> getGuests() {
        return guestPageKeyedDataSource.getGuests();
    }


    public void refresh(){
        guestPageKeyedDataSource.invalidate();
    }
}
