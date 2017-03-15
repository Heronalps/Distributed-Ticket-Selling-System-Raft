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

    @Override
    public boolean equals(Object entry){

        return this.clientId.equals(((LogEntry) entry).clientId)
                && this.requestId == ((LogEntry) entry).requestId;
    }

    @Override
    public String toString() {
        return "Request ID : " + String.valueOf(requestId) + " "
                + clientId + " wants to buy " + String.valueOf(numOfTicket) + " tickets";
    }

    LogEntry() {

    }

    LogEntry(int term, int index, int numOfTicket, String clientId, int requestId, int clientPort){
        this.term = term;
        this.index = index;
        this.numOfTicket = numOfTicket;
        this.clientId = clientId;
        this.requestId = requestId;
        this.clientPort = clientPort;
    }
}
