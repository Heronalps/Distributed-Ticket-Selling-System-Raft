package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 3/11/17.
 */
public class AppendEntries {
    int term;
    String leadId;
    int prevIndex; // Exactly the index of latest log entry
    int prevTerm;
    LogEntry entry; //log entries to store, empty for heartbeat
    int commitIndex;
    int leaderPort;

    public AppendEntries(int term,
                         String leadId,
                         int prevIndex, // Exactly the index of latest log entry
                         int prevTerm,
                         LogEntry entry, //log entries to store, empty for heartbeat
                         int commitIndex,
                         int leaderPort){
        this.term = term;
        this.leadId = leadId;
        this.prevIndex = prevIndex;
        this.prevTerm = prevTerm;
        this.entry = entry;
        this.commitIndex = commitIndex;
        this.leaderPort = leaderPort;
    }
}
