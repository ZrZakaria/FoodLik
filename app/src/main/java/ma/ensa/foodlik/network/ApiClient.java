package ma.ensa.foodlik.network;

import android.content.Context;

import java.util.concurrent.TimeUnit;

import ma.ensa.foodlik.BuildConfig;
import ma.ensa.foodlik.data.PreferencesManager;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static String currentBaseUrl;
    private static FoodLikApi api;

    private ApiClient() {
    }

    public static synchronized FoodLikApi get(Context context) {
        String baseUrl = new PreferencesManager(context).getApiBaseUrl();
        if (!baseUrl.endsWith("/")) {
            baseUrl = baseUrl + "/";
        }
        if (api == null || !baseUrl.equals(currentBaseUrl)) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG_LOGGING ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new MockBackendInterceptor(context))
                    .addInterceptor(logging)
                    .build();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            api = retrofit.create(FoodLikApi.class);
            currentBaseUrl = baseUrl;
        }
        return api;
    }
}
