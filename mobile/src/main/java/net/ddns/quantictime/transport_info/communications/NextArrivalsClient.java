package net.ddns.quantictime.transport_info.communications;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.ddns.quantictime.transport_info.business_object.RequestDetail;
import net.ddns.quantictime.transport_info.business_object.StaticInfo;
import net.ddns.quantictime.transport_info.business_object.StaticInfoLoader;
import net.ddns.quantictime.transport_info.business_object.Station;
import net.ddns.quantictime.transport_info.util.Constants;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
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

    public Observable<Station> getNextArrivals(@NonNull final List<String> stops, final List<String> fileNames, final Activity ctx, final List<String> lineBounds) {
        Observable<Station> observableStations=Observable.empty();
        for(int i=0;i<stops.size();i++)
            observableStations = observableStations.concatWith(service.getNextArrivals(stops.get(i))
                    .onErrorResumeNext(getAheadTime(ctx,stops.get(i), lineBounds.get(i)).concatWith(StaticInfoLoader.getListBusInfo(ctx, fileNames.get(i)).map(new Func1<Station, Station>() {
                                @Override
                                public Station call(Station station) {
                                    SharedPreferences settings = ctx.getPreferences(0);
                                    int minutes=settings.getInt("minutes",0);
                                    Calendar currentCal = Calendar.getInstance();
                                    currentCal.add(Calendar.MINUTE, minutes);
                                    DateFormat df = new SimpleDateFormat("HH:mm");
                                    Calendar cal = Calendar.getInstance();
                                    int j = 1;
                                    try {
                                        cal.setTime(df.parse(station.getLines()[0].getWaitTime()));
                                        cal.set(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DATE));
                                        while (j < station.getLines().length && cal.before(currentCal)) {
                                            cal.setTime(df.parse(station.getLines()[j].getWaitTime()));
                                            cal.set(currentCal.get(Calendar.YEAR), currentCal.get(Calendar.MONTH), currentCal.get(Calendar.DATE));
                                            j++;
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                    if (j < station.getLines().length)
                                        return new Station(Arrays.copyOfRange(station.getLines(), j - 1, station.getLines().length), station.getStopName());
                                    else
                                        return new Station(new RequestDetail[0], station.getStopName());
                                }
                            })
                    ).reduce((x,y)->{
                        RequestDetail[] result = Arrays.copyOf(x.getLines(), x.getLines().length + y.getLines().length);
                        System.arraycopy(y.getLines(), 0, result, x.getLines().length, y.getLines().length);
                        return new Station(result, y.getStopName());})));
        return observableStations;
    }

    private Observable<Station> getAheadTime(Activity ctx, String station, String lineBound){
        SharedPreferences settings = ctx.getPreferences(0);
        String times=settings.getString(Constants.myMap.get(station),null);
        Long time=Calendar.getInstance().getTimeInMillis();
        Long timestamp=settings.getLong(Constants.myMap.get(station)+"H", time);
        final int minutes=new Long((time-timestamp)/60000).intValue();
        SharedPreferences.Editor editor = settings.edit();
        List<RequestDetail> lista = times==null?new ArrayList<>():Observable.from(times.split(" "))
          .filter(x->x.matches("\\d+"))
          .map(x->Integer.parseInt(x)-minutes)
          .filter(x->x>0)
          .map(x->new RequestDetail(x.toString()+" min", lineBound))
          .toList().toBlocking().single();
        if(!lista.isEmpty())
            editor.putInt("minutes", Integer.parseInt(lista.get(lista.size()-1).getWaitTime().split(" ")[0]));
        else
            editor.putInt("minutes", 0);
        editor.commit();
        return Observable.just(new Station(lista.toArray(new RequestDetail[lista.size()]), "** "+Constants.myMap.get(station)+" (E)**"));
    }
}
