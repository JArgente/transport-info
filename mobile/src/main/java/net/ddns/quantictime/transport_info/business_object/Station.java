package net.ddns.quantictime.transport_info.business_object;

/**
 * Created by jorge on 16/01/2018.
 */

public class Station {

    private final RequestDetail[] lines;
    private final String stopName;

    public Station(RequestDetail[] lines, String stopName){
        this.lines=lines; this.stopName=stopName;
    }

    public RequestDetail[] getLines(){
        return this.lines;
    }

    public String getStopName(){
        return this.stopName;
    }

}
