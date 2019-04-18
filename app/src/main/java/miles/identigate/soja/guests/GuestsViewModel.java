package miles.identigate.soja.guests;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;
import android.util.Log;

import miles.identigate.soja.service.storage.model.Guest;

public class GuestsViewModel extends AndroidViewModel {
    LiveData<PagedList<Guest>> listLiveData;
    GuestsRepository guestsRepository;

    private static final String TAG = "GuestsViewModel";


    public GuestsViewModel(@NonNull Application application) {
        super(application);
        guestsRepository = new GuestsRepository(application);
    }

    public LiveData<PagedList<Guest>> getListLiveData() {
        listLiveData = guestsRepository.getGuests();
        Log.d(TAG, "getListLiveData: "+listLiveData.toString());
        return listLiveData;
    }
}
