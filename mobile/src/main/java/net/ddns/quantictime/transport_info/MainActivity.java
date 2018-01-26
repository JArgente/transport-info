package net.ddns.quantictime.transport_info;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import net.ddns.quantictime.transport_info.business_object.FinalDetail;
import net.ddns.quantictime.transport_info.business_object.RequestDetail;
import net.ddns.quantictime.transport_info.business_object.Station;
import net.ddns.quantictime.transport_info.communications.NextArrivalsClient;

import java.util.Arrays;
import java.util.List;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
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
                .map(new Func1<Station, FinalDetail>() {

                    @Override
                    public FinalDetail call(Station station) {
                        String arrivals="";
                        for(RequestDetail details: station.getLines()){
                            if(direction.contains(details.getLineBound()))
                                arrivals=arrivals+"  "+details.getWaitTime();
                        }
                        return new FinalDetail(station.getStopName(), arrivals);
                    }
                })
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
