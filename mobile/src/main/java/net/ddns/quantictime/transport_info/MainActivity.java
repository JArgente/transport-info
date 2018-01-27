package net.ddns.quantictime.transport_info;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import net.ddns.quantictime.transport_info.business_object.BusInfo;
import net.ddns.quantictime.transport_info.business_object.FinalDetail;
import net.ddns.quantictime.transport_info.business_object.InfoBusesLoader;
import net.ddns.quantictime.transport_info.business_object.RequestDetail;
import net.ddns.quantictime.transport_info.business_object.Station;
import net.ddns.quantictime.transport_info.communications.NextArrivalsClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rx.Notification;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RequestDetailAdapter adapter = new RequestDetailAdapter();
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.list_view_details);
        listView.setAdapter(adapter);

        getTransportDetails(Arrays.asList("10-14","4-202","5-24"), Arrays.asList("Colonia Jardin", "Puerta Del Sur","Humanes", "Fuenlabrada"));

    }

    @Override
    protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void getTransportDetails(final List<String> stops, final List<String> direction) {
        subscription = NextArrivalsClient.getInstance()
                .getNextArrivals(stops)
                .flatMap(new Func1<Station, Observable<FinalDetail>>() {
                    @Override
                    public Observable<FinalDetail> call(Station station) {
                        final Station station2=station;
                       return Observable.from(station.getLines()).filter(new Func1<RequestDetail, Boolean>() {
                            @Override
                            public Boolean call(RequestDetail requestDetail) {
                                return direction.contains(requestDetail.getLineBound());
                            }
                        }).limit(3)
                         .reduce("", new Func2<String, RequestDetail, String>() {
                            @Override
                            public String call(String s, RequestDetail requestDetail) {
                                return s+"  "+requestDetail.getWaitTime();
                            }
                        }).map(new Func1<String, FinalDetail>() {
                           @Override
                           public FinalDetail call(String s) {
                               return new FinalDetail(station2.getStopName(), s);
                           }
                       });
                    }
                })
                .concatWith(InfoBusesLoader.getListBusInfo(this)
                        .filter(new Func1<BusInfo, Boolean>() {
                            @Override
                            public Boolean call(BusInfo busInfo) {
                                Calendar currentCal = Calendar.getInstance();
                                DateFormat df= new SimpleDateFormat("HH:mm");
                                Calendar cal=Calendar.getInstance();
                                try {
                                    cal.setTime(df.parse(busInfo.getHoraSalida()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                cal.set(currentCal.get(Calendar.YEAR),currentCal.get(Calendar.MONTH),currentCal.get(Calendar.DATE));
                                return cal.after(currentCal);
                            }
                        })
                .limit(3)
                        .reduce("", new Func2<String, BusInfo, String>() {
                            @Override
                            public String call(String s, BusInfo busInfo) {
                                return s+"  "+busInfo.getHoraSalida();
                            }
                        })
                .map(new Func1<String, FinalDetail>() {
                    @Override
                    public FinalDetail call(String busInfo) {
                        return new FinalDetail("Méndez Álvaro", busInfo);
                    }
                }))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FinalDetail>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "In onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "In onError()");
                    }

                    @Override public void onNext(FinalDetail details) {
                        Log.d(TAG, "In onNext()");
                        adapter.setNextArrivals(details);
                    }
                });
    }
}
