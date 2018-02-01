package net.ddns.quantictime.transport_info;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
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
    private final String GFT="gft";
    private final String FINCA="finca";
    private final String VAR_NAME="place";
    private final List<String> FINCA_PARADAS=Arrays.asList("10-14", "4-202", "5-24");
    private final List<List<String>> FINCA_DIRECCION=Arrays.asList(
            Arrays.asList("Colonia Jardin"),
            Arrays.asList("Puerta Del Sur"),
            Arrays.asList("Humanes", "Fuenlabrada"));
    private final List<String> GFT_PARADAS=Arrays.asList("5-64", "5-11");
    private final List<List<String>> GFT_DIRECCION=Arrays.asList(
            Arrays.asList("Aranjuez", "Alcalá De Henares", "Guadalajara"),
            Arrays.asList("Fuenlabrada", "Humanes", "Príncipe Pío"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Location finca = new Location(FINCA);
        final Location gft = new Location(GFT);
        finca.setLatitude(40.420006);
        finca.setLongitude(-3.8015323);

        gft.setLatitude(40.4900041);
        gft.setLongitude(-3.6914176);
        // Acquire a reference to the system Location Manager
        final LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            Location lastLocation=null;
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                if (lastLocation == null || lastLocation.distanceTo(location) > 100) {
                    lastLocation=location;
                    adapter.initialize();
                    SharedPreferences settings = getPreferences(0);
                    SharedPreferences.Editor editor = settings.edit();

                    if (location.distanceTo(finca) < 500) {
                        editor.putString(VAR_NAME, FINCA);
                        editor.commit();
                        getTransportDetails(FINCA_PARADAS, FINCA_DIRECCION);
                    } else if (location.distanceTo(gft) < 500) {
                        editor.putString(VAR_NAME, GFT);
                        editor.commit();
                        getTransportDetails(GFT_PARADAS, GFT_DIRECCION);
                    } else {
                        String place = settings.getString(VAR_NAME, GFT);
                        if (place.equals(FINCA))
                            getTransportDetails(FINCA_PARADAS, FINCA_DIRECCION);
                        else
                            getTransportDetails(GFT_PARADAS, GFT_DIRECCION);
                    }
                }
            }
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };

// Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);

        setContentView(R.layout.activity_main);

        final ListView listView = (ListView) findViewById(R.id.list_view_details);
        listView.setAdapter(adapter);

    }

    @Override
    protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void getTransportDetails(final List<String> stops, final List<List<String>> direction) {
        subscription = NextArrivalsClient.getInstance()
                .getNextArrivals(stops).retry(3)
                .zipWith(direction, new Func2<Station, List<String>, Pair<Station, List<String>>>() {
                    @Override
                    public Pair<Station, List<String>> call(Station station, List<String> strings) {
                        return new Pair<>(station, strings);
                    }
                })
                .flatMap(new Func1<Pair<Station, List<String>>, Observable<FinalDetail>>() {
                    @Override
                    public Observable<FinalDetail> call(Pair<Station, List<String>> station) {
                        final Pair<Station, List<String>> par=station;
                       return Observable.from(par.first.getLines()).filter(new Func1<RequestDetail, Boolean>() {
                            @Override
                            public Boolean call(RequestDetail requestDetail) {
                                return par.second.contains(requestDetail.getLineBound());
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
                               return new FinalDetail(par.first.getStopName(), s);
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
