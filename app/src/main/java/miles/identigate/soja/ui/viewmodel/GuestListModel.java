package miles.identigate.soja.ui.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import miles.identigate.soja.service.GuestsRepository;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;

public class GuestListModel extends AndroidViewModel {
    private GuestsRepository repository;


    public GuestListModel(@NonNull Application application) {
        super(application);
        repository = GuestsRepository.getInstance(application);
    }

    public LiveData<PagedList<Guest>> getGuests() {
        return repository.getGuests();
    }

    public LiveData<NetworkState> getNetworkState() {
        return repository.getNetworkState();
    }

    public void refresh() {
        repository.refresh();
    }

}
