package net.ddns.quantictime.transport_app.communications;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ddns.quantictime.transport_app.business_object.Station;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by jorge on 16/01/2018.
 */

public class NextArrivalsClient {
    private static final String TRANSPORT_BASE_URL = "https://api.interurbanos.welbits.com/v1/";

    private static NextArrivalsClient instance;
    private NextArrivalsService service;

    private NextArrivalsClient() {
        final Gson gson =
                new GsonBuilder().create();
        final Retrofit retrofit = new Retrofit.Builder().baseUrl(TRANSPORT_BASE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();
        service = retrofit.create(NextArrivalsService.class);
    }

    public static NextArrivalsClient getInstance() {
        if (instance == null) {
            instance = new NextArrivalsClient();
        }
        return instance;
    }

    public Observable<Station> getNextArrivals(@NonNull String stop) {
        return service.getNextArrivals(stop);
    }
}