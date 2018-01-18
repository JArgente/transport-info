package net.ddns.quantictime.transport_app.communications;

import net.ddns.quantictime.transport_app.business_object.Station;

import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by jorge on 16/01/2018.
 */

public interface NextArrivalsService {
    @GET("stop/{stop}")
    Observable<Station> getNextArrivals(@Path("stop") String userName);
}
