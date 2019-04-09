package miles.identigate.soja.service.network.api;

import java.util.ArrayList;

import miles.identigate.soja.service.storage.model.Guest;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @POST("get_guests")
    Call<ArrayList<Guest>> getGuests(
            @Field("premiseZoneID") String premiseZoneID

    );
}
