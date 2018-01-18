package net.ddns.quantictime.transport_app.business_object;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Local unit test to test the correctness of StationTest POJO class
 */
public class StationTest {
    private static String STATION_NAME= "name";
    private static List STATION_NEXT_ARRIVES= Arrays.asList(1,2,3);
    private static String STATION_PREVIOUS= "previous";
    private static Integer STATION_ESTIMATED_TIME= 6;

    @Test
    public void station_isCorrect() throws Exception {
        Station station= new Station(STATION_NAME, STATION_NEXT_ARRIVES, STATION_PREVIOUS, STATION_ESTIMATED_TIME);

        assertEquals(STATION_NAME, station.getName());
        assertEquals(STATION_NEXT_ARRIVES, station.getListNextArrives());
        assertEquals(STATION_PREVIOUS, station.getPreviousStation());
        assertEquals(STATION_ESTIMATED_TIME, station.getEstimatedTime());
    }
}