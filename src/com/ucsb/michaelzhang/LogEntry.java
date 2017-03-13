package com.ucsb.michaelzhang;

import java.io.Serializable;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class LogEntry implements Serializable{
    int term;
    int index;
    int numOfTicket;
    String clientId;
    int requestId;
    int clientPort;
    boolean isConfigChange;

    @Override
    public boolean equals(Object entry){

        return this.clientId.equals(((LogEntry) entry).clientId)
                && this.requestId == ((LogEntry) entry).requestId;
    }

    @Override
    public String toString() {
        return String.valueOf(requestId) + " " + clientId + " wants to buy " + String.valueOf(numOfTicket) + " tickets";
    }

    LogEntry(int term, int index, int numOfTicket, String clientId, int requestId, int clientPort, boolean isConfigChange){
        this.term = term;
        this.index = index;
        this.numOfTicket = numOfTicket;
        this.clientId = clientId;
        this.requestId = requestId;
        this.clientPort = clientPort;
        this.isConfigChange = isConfigChange;
    }
}
