package net.ddns.quantictime.transport_app;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import net.ddns.quantictime.transport_app.business_object.RequestDetail;
import net.ddns.quantictime.transport_app.business_object.Station;
import net.ddns.quantictime.transport_app.communications.NextArrivalsClient;

import rx.Observable;
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

        getTransportDetails("10-14");
    }

    @Override
    protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void getTransportDetails(String stop) {
        subscription = NextArrivalsClient.getInstance()
                .getNextArrivals(stop)
                .flatMap(new Func1<Station, Observable<RequestDetail>>() {
                    @Override
                    public Observable<RequestDetail> call(Station station) {
                        return Observable.from(station.getLines());
                    }
                })
                .filter(new Func1<RequestDetail, Boolean>() {

                    @Override
                    public Boolean call(RequestDetail o) {
                        return o.getLineBound().equals("Colonia Jardin");
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<RequestDetail>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "In onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.d(TAG, "In onError()");
                    }

                    @Override public void onNext(RequestDetail details) {
                        Log.d(TAG, "In onNext()");
                        adapter.setNextArrivals(details);
                    }
                });
    }
}
