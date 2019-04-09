package miles.identigate.soja.service.storage.paging;

import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

import miles.identigate.soja.service.storage.GuestDao;
import miles.identigate.soja.service.storage.model.Guest;

public class DBGuestPageKeyedDataSource extends PageKeyedDataSource<String, Guest> {
    private static final String TAG = "DBGuestPageKeyedDataSou";
    private final GuestDao guestDao;


    public DBGuestPageKeyedDataSource(GuestDao dao) {
        guestDao = dao;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, Guest> callback) {
        Log.i(TAG, "loadInitial: Load Initial Range,Count " + params.requestedLoadSize);

        List<Guest> guests = guestDao.getAll();

        if (guests.size() != 0) {
            callback.onResult(guests, "0", "1");
        }
    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Guest> callback) {

    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Guest> callback) {

    }
}
