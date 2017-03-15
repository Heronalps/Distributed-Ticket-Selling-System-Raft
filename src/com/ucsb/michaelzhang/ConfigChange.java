package com.ucsb.michaelzhang;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaelzhang on 2/23/17.
 */
public class ConfigChange extends LogEntry {

    HashMap<String, Integer> oldDataCenterMap;
    HashMap<String, Integer> newDataCenterMap;
    boolean upOrDown; // true for up, false for down

    public ConfigChange(int term, int index, boolean upOrDown,
                        HashMap<String, Integer> oldDataCenterMap, HashMap<String, Integer> newDataCenterMap) {
        this.oldDataCenterMap = oldDataCenterMap;
        this.newDataCenterMap = newDataCenterMap;
        this.upOrDown = upOrDown;
        this.term = term;
        this.index = index;
    }

    @Override
    public String toString() {

        StringBuilder oldSB = new StringBuilder();
        for (Map.Entry entry : oldDataCenterMap.entrySet()) {
            oldSB.append(entry.getKey() + " ");
        }
        StringBuilder newSB = new StringBuilder();
        for (Map.Entry entry : newDataCenterMap.entrySet()) {
            newSB.append(entry.getKey() + " ");
        }

        return "Configuration Change From [ " +  oldSB.toString() + " ] to [ " + newSB.toString() + " ] ...";
    }
}
