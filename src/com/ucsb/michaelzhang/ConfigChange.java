package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 2/23/17.
 */
public class ConfigChange extends LogEntry {


    ConfigChange(int term, int index, int numOfTicket, String clientId, int requestId, int clientPort, boolean isConfigChange) {
        super(term, index, numOfTicket, clientId, requestId, clientPort, isConfigChange);
    }
}
