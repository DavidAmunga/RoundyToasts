package miles.identigate.soja.search.guests;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import miles.identigate.soja.guests.GuestsRepository;
import miles.identigate.soja.service.storage.model.Guest;

public class SearchGuestsViewModel extends AndroidViewModel {
    LiveData<PagedList<Guest>> listLiveData;
    SearchGuestsRepository searchGuestsRepository;

    String query;

    public void setSearchQuery(String query) {
        this.query = query;
    }


    public SearchGuestsViewModel(@NonNull Application application) {
        super(application);
        searchGuestsRepository = new SearchGuestsRepository(application);
    }

    public LiveData<PagedList<Guest>> getListLiveData() {
        listLiveData = searchGuestsRepository.getSearchResults(this.query);
        return listLiveData;
    }
}
