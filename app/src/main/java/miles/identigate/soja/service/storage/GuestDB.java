package miles.identigate.soja.service.storage;

import android.arch.lifecycle.LiveData;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import miles.identigate.soja.service.storage.model.Guest;
import miles.identigate.soja.service.storage.paging.DBGuestDataSourceFactory;

import static miles.identigate.soja.helpers.Constants.DATABASE_NAME;
import static miles.identigate.soja.helpers.Constants.NUMBERS_OF_THREADS;

@Database(entities = {Guest.class},version = 1)
public abstract class GuestDB extends RoomDatabase {

    private static GuestDB instance;

    private LiveData<PagedList<Guest>> guestsPaged;


    public abstract GuestDao guestDao();

    private static final Object sLock=new Object();

    public static GuestDB getInstance(Context context){
        synchronized (sLock){
            if(instance==null){
                instance=Room.databaseBuilder(context.getApplicationContext(),
                GuestDB.class,DATABASE_NAME)
                .build();

                instance.init();
            }
        }

        return instance;
    }

    private void init(){
        PagedList.Config pagedListConfig=(new PagedList.Config.Builder()).setEnablePlaceholders(false)
                .setInitialLoadSizeHint(Integer.MAX_VALUE).setPageSize(Integer.MAX_VALUE).build();

        Executor executor = Executors.newFixedThreadPool(NUMBERS_OF_THREADS);
        DBGuestDataSourceFactory dataSourceFactory = new DBGuestDataSourceFactory(guestDao());
        LivePagedListBuilder livePagedListBuilder = new LivePagedListBuilder<>(dataSourceFactory, pagedListConfig);


        guestsPaged=livePagedListBuilder.setFetchExecutor(executor).build();

    }

    public LiveData<PagedList<Guest>> getGuests(){
        return guestsPaged;
    }


}
