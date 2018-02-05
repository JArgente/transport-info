package net.ddns.quantictime.transport_info.business_object;


import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import rx.Observable;

/**
 * Created by jorge on 26/01/2018.
 */

public class StaticInfoLoader {

    public static Observable<Station> getListBusInfo(Context ctx, String name) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        InputStream is=null;
        try {
            is= ctx.getAssets().open(name);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Station lista=gson.fromJson(new InputStreamReader(is), Station.class);
        return Observable.just(lista);
    }
}
