package net.ddns.quantictime.transport_info.business_object;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;

/**
 * Created by jorge on 26/01/2018.
 */

public class InfoBusesLoader {

    public static Observable<BusInfo> getListBusInfo(Context ctx) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        InputStream is=null;
        try {
            is= ctx.getAssets().open("infoBuses.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
        BusInfo[] lista=gson.fromJson(new InputStreamReader(is), BusInfo[].class);
        return Observable.from(lista);
    }
}
