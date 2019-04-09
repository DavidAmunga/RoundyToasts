package miles.identigate.soja.service.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

import miles.identigate.soja.service.storage.model.Guest;

@Dao
public interface GuestDao {


    @Query("SELECT * FROM guest")
    List<Guest> getAll();


    @Query("SELECT COUNT(*) from guest")
    int countUsers();



//    Insert a guest in the database. If already exists, replace it.
//    @param guest is the movie to be inserted


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertGuest(Guest guest);



    @Query("DELETE FROM guest")
    abstract void deleteAllGuests();


    @Delete
    void delete(Guest guest);


}
