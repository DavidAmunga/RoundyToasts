package miles.identigate.soja.service.network.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import miles.identigate.soja.app.Common;
import miles.identigate.soja.helpers.Preferences;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    public static APIInterface getClient(Preferences preferences, String list) {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // create OkHttpClient and register an interceptor
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();


        Gson gson = null;

        if (list.equals(Common.GUEST_LIST)) {
            gson = new GsonBuilder()
                    // we remove from the response some wrapper tags from our Guest array
                    .registerTypeAdapter(Common.GUEST_ARRAY_LIST_CLASS_TYPE, new APIJsonSerializer(Common.GUEST_LIST))
                    .create();

        } else {
            gson = new GsonBuilder()
                    .create();

        }


        Retrofit.Builder builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .baseUrl(preferences.getBaseURL());


        return builder.build().create(APIInterface.class);
    }
}
