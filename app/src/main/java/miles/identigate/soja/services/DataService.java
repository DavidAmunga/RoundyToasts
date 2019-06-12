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

    @FormUrlEncoded
    @POST("record-visit")
    Call<String> recordGuest(
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


}
