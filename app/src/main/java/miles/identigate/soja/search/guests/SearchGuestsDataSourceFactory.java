package miles.identigate.soja.search.guests;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.DataSource;
import android.content.Context;
import android.util.Log;

import miles.identigate.soja.service.storage.model.Guest;
import rx.subjects.ReplaySubject;

public class SearchGuestsDataSourceFactory extends DataSource.Factory {

    private static final String TAG = "SearchGuestsDataSourceF";

    private MutableLiveData<SearchGuestsDataSource> dataSourceMutableLiveData = new MutableLiveData<>();
    private SearchGuestsDataSource guestPageKeyedDataSource;
    String query;
    Context context;


    void setSearchQuery(String query) {
        this.query = query;
    }


    public SearchGuestsDataSourceFactory(Context context) {
        this.context=context;
    }


    @Override
    public DataSource create() {
        guestPageKeyedDataSource = new SearchGuestsDataSource(context, query);



        dataSourceMutableLiveData.postValue(guestPageKeyedDataSource);
        return guestPageKeyedDataSource;
    }


}
