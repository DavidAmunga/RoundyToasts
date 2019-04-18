package miles.identigate.soja.guests;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.content.Context;
import android.util.Log;

import miles.identigate.soja.service.storage.model.Guest;
import rx.subjects.ReplaySubject;

public class GuestsDataSourceFactory extends DataSource.Factory<Integer, Guest> {

    private static final String TAG = "SearchGuestsDataSourceF";

    private MutableLiveData<GuestsDataSource> guestsDataSourceMutableLiveData = new MutableLiveData<>();
    private GuestsDataSource dataSource;

    public GuestsDataSourceFactory(Context context) {
        dataSource = new GuestsDataSource(context);
    }

    @Override
    public DataSource<Integer, Guest> create() {
        Log.d(TAG, "create: ");
        guestsDataSourceMutableLiveData.postValue(dataSource);
        return dataSource;
    }


//    public void refresh() {
//        guestPageKeyedDataSource.invalidate();
//    }
}
