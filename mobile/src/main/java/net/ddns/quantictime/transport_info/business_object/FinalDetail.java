package net.ddns.quantictime.transport_info.business_object;

/**
 * Created by jorge on 21/01/2018.
 */

public class FinalDetail {

    private final String name;
    private final String nextArrivals;


    public FinalDetail(String name, String nextArrivals){
        this.name=name;
        this.nextArrivals=nextArrivals;
    }

    public String getName() {
        return name;
    }

    public String getNextArrivals() {
        return nextArrivals;
    }
}
