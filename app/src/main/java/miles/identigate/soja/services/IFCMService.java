package miles.identigate.soja.services;


import miles.identigate.soja.models.FCMResponse;
import miles.identigate.soja.models.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IFCMService {

    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAxBIfklI:APA91bGYcvRUsim41oIQfd9dHqJKLDCzBweRj1wa8dDvv8ustY-K0i_vKSlazN2AF-hb2uzkdDP7Cv5_MjUFDirxozohLO4Hyc0VK30yJ7Uf6gtaFP6cCDyoChfw443MwdVH1_BEFrcN"
    })

    @POST("fcm/send")
    Call<FCMResponse> sendMessage(@Body Sender body);

}
