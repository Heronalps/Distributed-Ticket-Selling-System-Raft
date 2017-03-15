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
    boolean oldAndNew; // true for OLD + NEW, false for NEW

    public ConfigChange(int term, int index, boolean upOrDown, boolean oldAndNew,
                        HashMap<String, Integer> oldDataCenterMap, HashMap<String, Integer> newDataCenterMap) {
        this.oldDataCenterMap = oldDataCenterMap;
        this.newDataCenterMap = newDataCenterMap;
        this.upOrDown = upOrDown;
        this.oldAndNew = oldAndNew;
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

        String version = oldAndNew ? "OLD + NEW" : "NEW";

        return "Configuration Change " + version + " From [ " +  oldSB.toString() + " ] to [ " + newSB.toString() + " ] ...";
    }
}
