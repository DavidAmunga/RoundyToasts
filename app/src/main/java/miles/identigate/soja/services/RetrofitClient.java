package miles.identigate.soja.services;

import android.content.Context;

import miles.identigate.soja.helpers.Preferences;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


public class RetrofitClient {


    private static Retrofit retrofit = null;

    public static DataService getDataService(Context context) {
        Preferences preferences = new Preferences(context);


        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
            httpClient.addInterceptor(logging);  // <-- this is the important line!


            retrofit = new Retrofit.Builder()
                    .baseUrl(preferences.getBaseURL())
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }


        return retrofit.create(DataService.class);
    }

    public static DataService getDataObjService(Context context) {
        Preferences preferences = new Preferences(context);


        if (retrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
            httpClient.addInterceptor(logging);  // <-- this is the important line!


            retrofit = new Retrofit.Builder()
                    .baseUrl(preferences.getBaseURL())
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }


        return retrofit.create(DataService.class);
    }


}
