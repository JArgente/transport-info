package net.ddns.quantictime.transport_info.communications;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ddns.quantictime.transport_info.business_object.RequestDetail;
import net.ddns.quantictime.transport_info.business_object.StaticInfo;
import net.ddns.quantictime.transport_info.business_object.StaticInfoLoader;
import net.ddns.quantictime.transport_info.business_object.Station;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

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

    public Observable<Station> getNextArrivals(@NonNull final List<String> stops, final List<String> fileNames, Context ctx) {
        Observable<Station> observableStations=Observable.empty();
        for(int i=0;i<stops.size();i++)
            observableStations=observableStations.concatWith(service.getNextArrivals(stops.get(i)).onErrorResumeNext(
                    StaticInfoLoader.getListBusInfo(ctx, fileNames.get(i)).map(new Func1<Station, Station>() {
                        @Override
                        public Station call(Station station) {
                            Calendar currentCal = Calendar.getInstance();
                            DateFormat df= new SimpleDateFormat("HH:mm");
                            Calendar cal=Calendar.getInstance();
                            int i=1;
                            try {
                                cal.setTime(df.parse(station.getLines()[0].getWaitTime()));
                                cal.set(currentCal.get(Calendar.YEAR),currentCal.get(Calendar.MONTH),currentCal.get(Calendar.DATE));
                                while(i< station.getLines().length && cal.before(currentCal)){
                                    cal.setTime(df.parse(station.getLines()[i].getWaitTime()));
                                    cal.set(currentCal.get(Calendar.YEAR),currentCal.get(Calendar.MONTH),currentCal.get(Calendar.DATE));
                                    i++;
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                            if(i<station.getLines().length)
                                return new Station(Arrays.copyOfRange(station.getLines(), i, station.getLines().length), station.getStopName());
                            else
                                return new Station(new RequestDetail[0], station.getStopName());
                        }
                    })));
        return observableStations;
    }
}
