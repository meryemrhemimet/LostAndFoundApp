package com.example.myapplication.util;

import java.util.List;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

// Retrofit avec intercepteur bouchonné (mock) — pas de serveur réel nécessaire
public class RetrofitService {

    // Interface API
    public interface LostFoundApi {
        @GET("api/items")
        Call<List<Object>> getItems();
    }

    // Intercepteur mock : retourne du JSON statique
    private static final Interceptor MOCK = chain -> new Response.Builder()
        .code(200).message("OK").protocol(Protocol.HTTP_1_1).request(chain.request())
        .body(ResponseBody.create(MediaType.parse("application/json"),
            "[{\"id\":1,\"title\":\"Objet test\",\"type\":\"lost\"}]"))
        .build();

    private static Retrofit retrofit;

    public static LostFoundApi getApi() {
        if (retrofit == null) {
            OkHttpClient client = new OkHttpClient.Builder().addInterceptor(MOCK).build();
            retrofit = new Retrofit.Builder()
                .baseUrl("https://api.lostfound.local/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        }
        return retrofit.create(LostFoundApi.class);
    }
}
