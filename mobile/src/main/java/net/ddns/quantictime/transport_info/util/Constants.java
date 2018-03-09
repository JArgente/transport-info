package net.ddns.quantictime.transport_info.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jorge on 07/03/2018.
 */

public class Constants {
    public static final Map<String, String> myMap;
    static {
        Map<String, String> aMap = new HashMap<>();
        aMap.put("10-15", "Somosaguas Centro");
        aMap.put("4-202", "Colonia Jardin");
        aMap.put("5-24", "Cuatro Vientos");
        aMap.put("5-64", "Ramon y Cajal");
        aMap.put("5-11", "Atocha");
        myMap = Collections.unmodifiableMap(aMap);
    }
}
