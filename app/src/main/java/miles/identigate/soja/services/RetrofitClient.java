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
    private static Retrofit residentRetrofit = null;
    private static Retrofit eventRetrofit = null;

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

    public static DataService getResidentsDataService(Context context) {
        Preferences preferences = new Preferences(context);


        if (residentRetrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
            httpClient.addInterceptor(logging);  // <-- this is the important line!


            residentRetrofit = new Retrofit.Builder()
                    .baseUrl(preferences.getBaseURL().replace("visits", "residents"))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }


        return residentRetrofit.create(DataService.class);
    }

    public static DataService getEventsService(Context context) {
        Preferences preferences = new Preferences(context);


        if (eventRetrofit == null) {

            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
// set your desired log level
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
// add your other interceptors …

// add logging as last interceptor
            httpClient.addInterceptor(logging);  // <-- this is the important line!


            eventRetrofit = new Retrofit.Builder()
                    .baseUrl(preferences.getBaseURL().replace("visits", "events"))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(httpClient.build())
                    .build();
        }


        return eventRetrofit.create(DataService.class);
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
