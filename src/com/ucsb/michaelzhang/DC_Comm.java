package com.ucsb.michaelzhang;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by michaelzhang on 2/25/17.
 */
public interface DC_Comm extends Remote {


    // Client DC_Comm
    void handleRequest(int numOfTicket, String clientId, int requestId, int clientPort) throws RemoteException;

    ArrayList<LogEntry> fetchCommittedLogEntries() throws RemoteException;

    void change() throws RemoteException;
    //Configuration change command. Parameter list will be modified later.


    //Inter-DataCenter DC_Comm
    void handleRequestVote(String candidateId,
                         int term,
                         int lastLogIndex,
                         int lastLogTerm,
                         int myPort) throws RemoteException;

    void handleVote(int term,
                    boolean voteGranted,
                    String followerId) throws RemoteException;

    void handleAppendEntries(AppendEntries appendEntries) throws RemoteException;

    //Followers' Reply to AppendEntries
    void handleReply(int term,
                     boolean success,
                     int matchIndex,
                     String followerID) throws RemoteException;

    void handleConfigChange(boolean upOrDown,
                            boolean oldAndNew,
                            HashMap<String, Integer> oldDataCenterMap,
                            HashMap<String, Integer> newDataCenterMap,
                            String clientId,
                            int clientPort) throws RemoteException;

}
