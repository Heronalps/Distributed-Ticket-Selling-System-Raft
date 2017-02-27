package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class LogEntry {
    int term;
    int index;
    String command;

    public LogEntry(){

    }

    public LogEntry(int term, int index, String command){
        this.term = term;
        this.index = index;
        this.command = command;
    }
}
