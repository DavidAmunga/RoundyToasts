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


//        Gson gson = null;


        Retrofit.Builder builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .baseUrl(preferences.getBaseURL());


        return builder.build().create(APIInterface.class);
    }
}
