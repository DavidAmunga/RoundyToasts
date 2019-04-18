package miles.identigate.soja.services;


import miles.identigate.soja.models.QueryResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface DataService {

    @FormUrlEncoded
    @POST("record-visitor-exit")
    Call<QueryResponse> visitorExit(
            @Field("idNumber") String idNumber,
            @Field("deviceID") String deviceID,
            @Field("exitTime") String exitTime
    );



}
