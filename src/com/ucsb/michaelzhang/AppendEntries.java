package com.ucsb.michaelzhang;

import java.io.Serializable;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class AppendEntries extends Message implements Serializable {
    int term;
    String leadId;
    int prevLogIndex;
    int prevLogTerm;

    //log entries to store, empty for heartbeat
    LogEntry[] entries;
    int commitIndex;
}
