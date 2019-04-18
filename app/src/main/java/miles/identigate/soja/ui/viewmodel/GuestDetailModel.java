package miles.identigate.soja.ui.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.paging.PagedList;
import android.support.annotation.NonNull;

import miles.identigate.soja.service.GuestsRepository;
import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.model.NetworkState;

public class GuestDetailModel extends ViewModel {
    final private MutableLiveData guest;

    public GuestDetailModel() {
        guest = new MutableLiveData<Guest>();
    }

    public MutableLiveData getGuest() {
        return guest;
    }
}
