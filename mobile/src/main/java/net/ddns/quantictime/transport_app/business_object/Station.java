package net.ddns.quantictime.transport_app.business_object;

import android.content.Intent;

import java.util.List;

/**
 * Created by jorge on 16/01/2018.
 */

public class Station {

    private final RequestDetail[] lines;
    private final String stopNumber;

    public Station(RequestDetail[] lines, String stopNumber){
        this.lines=lines; this.stopNumber=stopNumber;
    }

    public RequestDetail[] getLines(){
        return this.lines;
    }

    public String getStopNumber(){
        return this.stopNumber;
    }

}
