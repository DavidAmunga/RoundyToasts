package miles.identigate.soja.service.storage.paging;

import android.arch.paging.DataSource;

import miles.identigate.soja.service.storage.GuestDao;

public class DBGuestDataSourceFactory extends DataSource.Factory {

    private static final String TAG = "DBGuestDataSourceFactor";

    private DBGuestPageKeyedDataSource dbGuestPageKeyedDataSource;

    public DBGuestDataSourceFactory(GuestDao guestDao) {
        dbGuestPageKeyedDataSource=new DBGuestPageKeyedDataSource(guestDao);
    }

    @Override
    public DataSource create() {
        return dbGuestPageKeyedDataSource;
    }
}
