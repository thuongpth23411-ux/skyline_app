package com.skyline.app;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FlightFilter implements Serializable {
    public List<String> airlineIds = new ArrayList<>();
    public int priceRangeIndex = -1; // -1: none, 0: <1.5, 1: 1.5-2.5, 2: 2.5-4, 3: >=4
    public int timeSlotIndex = -1;   // -1: none, 0: Sáng, 1: Trưa/Chiều, 2: Tối
    public int durationIndex = -1;   // -1: none, 0: <60, 1: 60-120, 2: >120

    public void reset() {
        airlineIds.clear();
        priceRangeIndex = -1;
        timeSlotIndex = -1;
        durationIndex = -1;
    }

    public boolean isEmpty() {
        return airlineIds.isEmpty() && priceRangeIndex == -1 && timeSlotIndex == -1 && durationIndex == -1;
    }
}
