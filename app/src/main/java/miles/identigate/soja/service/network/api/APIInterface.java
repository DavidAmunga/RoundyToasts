package miles.identigate.soja.service.network.api;

import java.util.ArrayList;

import miles.identigate.soja.models.QueryResponse;
import miles.identigate.soja.service.storage.model.Guest;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

    @GET("premise_residents")
    Call<ArrayList<Guest>> getGuests(
            @Query("premiseID") String premiseID,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize,
            @Query("search") String search
    );

    @FormUrlEncoded
    @POST("record-visit")
    Call<QueryResponse> recordGuest(
            @Field("houseID") String houseID,
            @Field("entryTime") String entryTime,
            @Field("birthDate") String birthDate,
            @Field("genderID") String genderID,
            @Field("firstName") String firstName,
            @Field("lastName") String lastName,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("designation") String designation,
            @Field("idNumber") String idNumber,
            @Field("idType") String idType,
            @Field("nationality") String nationality,
            @Field("nationCode") String nationCode,
            @Field("company") String company,
            @Field("visitType") String visitType,
            @Field("deviceID") String deviceID,
            @Field("premiseZoneID") String premiseZoneID,
            @Field("visitorTypeID") String visitorTypeID
    );


    @FormUrlEncoded
    @POST("record-visitor-exit")
    Call<QueryResponse> recordVisitExit(
            @Field("idNumber") String idNumber,
            @Field("deviceID") String deviceID,
            @Field("exitTime") String exitTime
    );

    @FormUrlEncoded
    @POST("residents_qr")
    Call<QueryResponse> getResidentsQR(
            @Field("hostID") String hostID,
            @Field("premiseID") String premiseID
    );


}
