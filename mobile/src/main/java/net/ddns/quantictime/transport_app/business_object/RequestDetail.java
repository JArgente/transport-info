package net.ddns.quantictime.transport_app.business_object;

/**
 * Created by jorge on 16/01/2018.
 */

public class RequestDetail {
    private final String waitTime;
    private final String lineBound;

    public RequestDetail(String waitTime, String lineBound){
        this.waitTime=waitTime;
        this.lineBound=lineBound;
    }

    public String getWaitTime() {
        return waitTime;
    }

    public String getLineBound() {
        return lineBound;
    }
}
