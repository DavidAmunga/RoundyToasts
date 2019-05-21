package miles.identigate.soja.services;


import miles.identigate.soja.models.QueryResponse;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface DataService {

    @FormUrlEncoded
    @POST("record-visitor-exit")
    Call<QueryResponse> visitorExit(
            @Field("idNumber") String idNumber,
            @Field("deviceID") String deviceID,
            @Field("exitTime") String exitTime
    );

    @GET("premise_invites")
    Call<String> getInvitees(
            @Query("premiseID") String premiseID,
            @Query("deviceID") String deviceID,
            @Query("page") Integer page,
            @Query("pageSize") Integer pageSize
    );

    @FormUrlEncoded
    @POST("record_invite")
    Call<String> recordInvite(
            @Query("houseID") String houseID,
            @Query("entryTime") String entryTime,
            @Query("birthDate") String birthDate,
            @Query("genderID") String genderID,
            @Query("firstName") String firstName,
            @Query("lastName") String lastName,
            @Query("email") String email,
            @Query("phone") String phone,
            @Query("designation") String designation,
            @Query("idNumber") String idNumber,
            @Query("idType") String idType,
            @Query("nationality") String nationality,
            @Query("nationCode") String nationCode,
            @Query("company") String company,
            @Query("visitType") String visitType,
            @Query("deviceID") String deviceID,
            @Query("premiseZoneID") String premiseZoneID,
            @Query("visitorTypeID") String visitorTypeID
    );


}
