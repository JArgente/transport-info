package net.ddns.quantictime.transport_info.business_object;

/**
 * Created by jorge on 26/01/2018.
 */

public class BusInfo {
    private String horaSalida;
    private String horaLlegada;

    public BusInfo(String horaSalida, String horaLlegada){
        this.horaSalida=horaSalida;
        this.horaLlegada=horaLlegada;
    }

    public String getHoraSalida() {
        return horaSalida;
    }

    public String getHoraLlegada() {
        return horaLlegada;
    }
}
