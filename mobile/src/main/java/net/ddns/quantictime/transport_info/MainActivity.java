package net.ddns.quantictime.transport_info;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.widget.ListView;

import net.ddns.quantictime.transport_info.business_object.FinalDetail;
import net.ddns.quantictime.transport_info.business_object.StaticInfoLoader;
import net.ddns.quantictime.transport_info.business_object.RequestDetail;
import net.ddns.quantictime.transport_info.business_object.Station;
import net.ddns.quantictime.transport_info.communications.NextArrivalsClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private RequestDetailAdapter adapter = new RequestDetailAdapter();
    private Subscription subscription;
    private final String GFT="gft";
    private final String FINCA="finca";
    private final String VAR_NAME="place";
    private final List<String> FINCA_PARADAS=Arrays.asList("10-15", "4-202", "5-24");
    private final List<List<String>> FINCA_DIRECCION=Arrays.asList(
            Arrays.asList("Colonia Jardin"),
            Arrays.asList("Puerta Del Sur"),
            Arrays.asList("Humanes", "Fuenlabrada"));
    private final List<String> GFT_PARADAS=Arrays.asList("5-64", "5-11");
    private final List<List<String>> GFT_DIRECCION=Arrays.asList(
            Arrays.asList("Aranjuez", "Alcalá De Henares", "Guadalajara"),
            Arrays.asList("Fuenlabrada", "Humanes", "Príncipe Pío"));

    private final List<String> FINCA_FILES=Arrays.asList("infoMLO.json","infoL10.json","infoC5.json");
    private final List<String> GFT_FILES=Arrays.asList("infoRyC.json","infoAtocha.json");

    Handler mHandler;
    Location location;
    final Location finca = new Location(FINCA);
    final Location gft = new Location(GFT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        finca.setLatitude(40.420006);
        finca.setLongitude(-3.8015323);

        gft.setLatitude(40.4900041);
        gft.setLongitude(-3.6914176);

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
                        getTransportDetails(FINCA_PARADAS, FINCA_FILES, FINCA_DIRECCION);
                    } else if (location.distanceTo(gft) < 500) {
                        editor.putString(VAR_NAME, GFT);
                        editor.commit();
                        getTransportDetails(GFT_PARADAS, GFT_FILES, GFT_DIRECCION);
                    } else {
                        String place = settings.getString(VAR_NAME, GFT);
                        if (place.equals(FINCA))
                            getTransportDetails(FINCA_PARADAS, FINCA_FILES, FINCA_DIRECCION);
                        else
                            getTransportDetails(GFT_PARADAS, GFT_FILES, GFT_DIRECCION);
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
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
        location= locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location.distanceTo(gft) < 500)
            getTransportDetails(GFT_PARADAS, GFT_FILES, GFT_DIRECCION);
        else
            getTransportDetails(FINCA_PARADAS, FINCA_FILES, FINCA_DIRECCION);


        final ListView listView = (ListView) findViewById(R.id.list_view_details);
        listView.setAdapter(adapter);
        this.mHandler = new Handler();

        this.mHandler.postDelayed(m_Runnable,60000);

    }

    private final Runnable m_Runnable = new Runnable()
    {
        public void run()

        {
            adapter.initialize();
            if (location.distanceTo(gft) < 500)
                getTransportDetails(GFT_PARADAS, GFT_FILES, GFT_DIRECCION);
            else
                getTransportDetails(FINCA_PARADAS, FINCA_FILES, FINCA_DIRECCION);

            mHandler.postDelayed(m_Runnable, 60000);
        }

    };

    @Override
    protected void onDestroy() {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
        super.onDestroy();
    }

    private void getTransportDetails(final List<String> stops, final List<String> files, final List<List<String>> direction) {
        final SharedPreferences settings=this.getPreferences(0);
        subscription = NextArrivalsClient.getInstance()
                .getNextArrivals(stops, files, this, direction.stream().flatMap(x->x.stream().limit(1)).collect(Collectors.toList())).timeout(3000, TimeUnit.MILLISECONDS).retry(3)
                .zipWith(direction, (station, lista)->new Pair<>(station, lista))
                .flatMap(par->Observable.from(par.first.getLines())
                        .filter(requestDetail->par.second.contains(requestDetail.getLineBound()))
                        .limit(3)
                        .reduce("", (s, requestDetail)->s+"  "+requestDetail.getWaitTime())
                        .map(s-> new FinalDetail(par.first.getStopName(), s)))
                .doOnNext(station->{
                                  if(station.getName().charAt(0)!='*') {
                                      final SharedPreferences.Editor editor = settings.edit();
                                      editor.putLong(station.getName() + "H", Calendar.getInstance().getTimeInMillis());
                                      editor.putString(station.getName(), station.getNextArrivals());
                                      editor.commit();
                                  }
                              }
                 )
                .concatWith(StaticInfoLoader.getListBusInfo(this, "infoBuses.json")
                        .map(station->{
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
                                    return new Station(Arrays.copyOfRange(station.getLines(), i-1, station.getLines().length), station.getStopName());
                                else
                                    return new Station(new RequestDetail[0], station.getStopName());
                            })
                        .limit(3)
                        .flatMap(par->Observable.from(par.getLines())
                                        .limit(3)
                                        .reduce("", (s, requestDetail)->s+"  "+requestDetail.getWaitTime())
                                        .map(s->new FinalDetail(par.getStopName(), s)))
                )
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
