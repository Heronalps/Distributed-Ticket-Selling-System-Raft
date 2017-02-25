package com.ucsb.michaelzhang;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by michaelzhang on 2/25/17.
 */
public interface Communication extends Remote {

    // Client Communication
    boolean buy(int numOfTicket) throws RemoteException;

    void show() throws RemoteException;
    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.


    void change() throws RemoteException;
    //Configuration change command. Parameter list will be modified later.


    //Inter-DataCenter Communication
    void sendRequestVote(String candidateId,
                         int term,
                         int lastLogIndex,
                         int lastLogTerm) throws RemoteException;

    void sendVote(int term,
                  boolean voteGranted) throws RemoteException;

    void sendAppendEntries(int term,
                           String leadId,
                           int prevLogIndex,
                           int prevLogTerm,
                           LogEntry[] entries, //log entries to store, empty for heartbeat
                           int commitIndex) throws RemoteException;

    //Followers' Reply to AppendEntries
    void sendACK(int term,
                 boolean success) throws RemoteException;

}
