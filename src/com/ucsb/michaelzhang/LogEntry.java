package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class LogEntry{
    int term;
    int index;
    int numOfTicket;
    String clientId;
    int requestId;
    int clientPort;


    LogEntry(int term, int index, int numOfTicket, String clientId, int requestId, int clientPort){
        this.term = term;
        this.index = index;
        this.numOfTicket = numOfTicket;
        this.clientId = clientId;
        this.requestId = requestId;
        this.clientPort = clientPort;
    }
}
