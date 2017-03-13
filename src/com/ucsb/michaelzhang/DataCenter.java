package com.ucsb.michaelzhang;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static com.ucsb.michaelzhang.Configuration.changeProperty;
import static com.ucsb.michaelzhang.Configuration.readConfig;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class DataCenter extends UnicastRemoteObject implements DC_Comm {

    static final int HEARTBEAT_INTERVAL = 5 * 1000;
    static final int ELECTION_INTERVAL = 1 * 10 * 1000;
    int currentTerm;
    String voteFor;
    ArrayList<LogEntry> logEntries; //logEntries is 1-based, instead of zero-based
    Role currentRole;
    String dataCenterId; //D1, D2, D3 ...
    Timer timer;
    int port;
    static int majority; //Current majority of network
    int lastLogIndex; //The index of last added log entry
    int lastLogTerm; // The term of last added log entry
    int committedIndex;
    int committedEntryCounter;
    int totalNumOfDataCenter;
    static Map<String, Integer> matchIndexMap = new HashMap<>();
    static Map<String, Integer> nextIndexMap = new HashMap<>();
    static Map<String, Boolean> voteMap = new HashMap<>();

    /*
     * public void handlerequest(int numOfTicket, String clientId, int requestId) throws RemoteException;
     *
     * public void show() throws RemoteException;
     *
     * public void change() throws RemoteException;
     *
     * public void handleRequestVote(String candidateId, //Sender's data center ID
                                int term,
                                int lastLogIndex,
                                int lastLogTerm,
                                int myPort) throws RemoteException;

     * public void handleVote(int term,
                         boolean voteGranted) throws RemoteException;

     * public void handleAppendEntries(int term,
                                    String leadId,
                                    int prevLogIndex, // Exactly the index of latest log entry
                                    int prevLogTerm,
                                    LogEntry entry, //log entries to store, empty for heartbeat
                                    int commitIndex,
                                    int leaderPort) throws RemoteException;

     * public void handleReply(int term,
                            boolean success,
                            int matchIndex) throws RemoteException;

     * private void reply(boolean isSuccess, int matchIndex, int leaderPort, String leaderId);
     *
     * private void vote(boolean isVote, int myPort, String candidateId);
     *
     * private void broadcastAppendEntries() throws IOException;
     *
     * private void broadcastRequestVote() throws IOException;
     *
    */


    public enum Role{
        Follower, Candidate, Leader
    }

    private DataCenter(String dataCenterId, int port) throws RemoteException{
        System.out.println(dataCenterId + " has been established...");
        this.currentRole = Role.Follower;
        this.dataCenterId = dataCenterId;
        this.port = port;
        this.logEntries = new ArrayList<>();
        this.currentTerm = 1;
        this.lastLogIndex = 0;
        this.lastLogTerm = 0;
        this.committedIndex = 0;
        this.committedEntryCounter = 1;


        // Only Config change log entry has the right to modify config files.

        try{
            totalNumOfDataCenter = Integer.parseInt(readConfig("Config_" + dataCenterId, "TotalNumOfDataCenter"));
            majority = totalNumOfDataCenter/ 2 + 1;

        } catch (IOException ex){
            ex.printStackTrace();
        }

        // Initialize nextIndexMap and matchIndexMap to full content

        for (int i = 1; i <= totalNumOfDataCenter; i++) {
            matchIndexMap.put("D" + i, 0);
            nextIndexMap.put("D" + i, 1);
        }
    }

    // Client DC Communication
    public void handleRequest(int numOfTicket,
                              String clientId,
                              int requestId,
                              int clientPort,
                              boolean isConfigChange) throws RemoteException {

        LogEntry logEntry = null;

        if (!isConfigChange) {
            logEntry = new LogEntry(currentTerm, lastLogIndex + 1, numOfTicket,
                    clientId, requestId, clientPort, false);

            // To prevent duplicate request

            for (LogEntry entry : logEntries) {
                if (logEntry.equals(entry)) {
                    return;
                }
            }

            System.out.println("Received a request from " + clientId + " to buy " + numOfTicket + " tickets ...");
            logEntries.add(logEntry);
            lastLogIndex++;
            lastLogTerm = logEntry.term;

        } else { // The request is a configuration change
            //TODO
        }


    }



    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.

    public void show() throws RemoteException{

    }


    //Config change command. Parameter list will be modified later.
    public void change() throws RemoteException{

    }



    //Inter-DataCenter Communication
    public void handleRequestVote(String candidateId, //Sender's data center ID
                                int term,
                                int lastLogIndex,
                                int lastLogTerm,
                                int myPort) throws RemoteException{

        System.out.println("Received RequestVote from " + candidateId);
        //sendVote()
        if (term < currentTerm) {
            System.out.println("Reply with false because " + candidateId +"'s term is lower than mine...");
            sendVote(false, myPort, candidateId);

        } else if(term > currentTerm) {
            updateTerm(term);
            if(this.currentRole == Role.Candidate || this.currentRole == Role.Leader) {
                becomeFollower();
            }
            if (lastLogTerm > this.lastLogTerm || (lastLogTerm == this.lastLogTerm && lastLogIndex >= this.lastLogIndex)){

                resetTimer(); //Only reset timer when I vote true.
                voteFor = candidateId;
                sendVote(true, myPort, candidateId);



            } else {
                voteFor = null;
                sendVote(false, myPort, candidateId);
            }
        } else if (term == currentTerm){
            if ((voteFor == null || voteFor.equals(candidateId))
                    && lastLogTerm > this.lastLogTerm || (lastLogTerm == this.lastLogTerm && lastLogIndex >= this.lastLogIndex)) {

                resetTimer();
                sendVote(true, myPort, candidateId);
            }
        }
    }

    public void handleVote(int term,
                         boolean voteGranted,
                           String followerId) throws RemoteException {

        if (term <= currentTerm) {
            if (voteGranted) {
                voteMap.put(followerId, true);
            }
            int numOfVotes = 0;
            for (Map.Entry<String, Boolean> entry : voteMap.entrySet()) {
                if (entry.getValue()) {
                    numOfVotes++;
                }
            }
            if (numOfVotes >= majority) {
                becomeLeader();
                voteMap.clear();
            }
        } else {
            updateTerm(term);
            becomeFollower();
        }
    }

    public void handleAppendEntries(AppendEntries appendEntries) throws RemoteException {

        System.out.println("Received AppendEntries from " + appendEntries.leadId);

        if (appendEntries.term > currentTerm) {
            updateTerm(appendEntries.term);
            if (this.currentRole == Role.Candidate || this.currentRole == Role.Leader) {
                becomeFollower();
            }

            reply(true, 0, appendEntries.leaderPort, appendEntries.leadId);
        }

        else if (appendEntries.term < currentTerm){

            // The reply will contain currentTerm, which will make sender to step down as follower.

            reply(false, 0, appendEntries.leaderPort, appendEntries.leadId);
        }

        else if (appendEntries.term == currentTerm) {

            if (this.currentRole == Role.Candidate || this.currentRole == Role.Leader) {
                becomeFollower();
                reply(true, 0, appendEntries.leaderPort, appendEntries.leadId);
            }
            else if (this.currentRole == Role.Follower) {

                // The previous index to prevIndex in LogEntries

                int prevLogIndex = appendEntries.prevIndex - 1;

                //Handling HeartBeat

                if (lastLogIndex == appendEntries.prevIndex && lastLogTerm == appendEntries.prevTerm){

                    //Only Reset the Timer when replying true
                    resetTimer();

                    if (appendEntries.commitIndex > committedIndex) {
                        commitLogEntry(appendEntries.commitIndex);
                    }
                    System.out.println("Handling HeartBeat ...");
                    reply(true, lastLogIndex, appendEntries.leaderPort, appendEntries.leadId);
                    showLogEntries();
                }

                //5. Return failure if log doesn't contain an entry at prevLogIndex whose term matches prevLogTerm

                else if (appendEntries.prevIndex != 0
                        && (appendEntries.prevIndex == 1
                        || (logEntries.size() >= prevLogIndex
                        && logEntries.get(prevLogIndex).term == appendEntries.prevPrevTerm))) {

                    resetTimer();
                    if (logEntries.size() > prevLogIndex) {
                        logEntries.subList(prevLogIndex + 1, logEntries.size()).clear();
                    }

                    logEntries.add(appendEntries.entry);
                    lastLogIndex++;
                    lastLogTerm = appendEntries.prevTerm;

                    System.out.println("Add new Entry ...");

                    reply(true, appendEntries.prevIndex, appendEntries.leaderPort, appendEntries.leadId);

                }

                else {

                    reply(false, 0, appendEntries.leaderPort, appendEntries.leadId);
                }
            }
        }
    }

    //Followers' Reply to AppendEntries
    public void handleReply(int term,
                            boolean success,
                            int matchIndex,
                            String followerId) throws RemoteException{

        if (term > currentTerm) {
            updateTerm(term);
            becomeFollower();
        } else if (term == currentTerm && success) {

            //Update next index in matchIndexMap if successful

            matchIndexMap.put(followerId, matchIndex);
            nextIndexMap.put(followerId, matchIndex + 1);

            /*
            System.out.println("matchIndex : " + matchIndex);
            for (Map.Entry<String, Integer> entry : nextIndexMap.entrySet()) {
                System.out.println("nextIndex of " + entry.getKey() + " : " + entry.getValue());
            }
            */


            // Then, we are going to commit the next log entry
            // This is an ongoing process in every reply handling, until minMatchIndex == committedIndex

            int nextCommittedIndex = committedIndex + 1;

            System.out.println("committedIndex : " + committedIndex);
            System.out.println("nextCommittedIndex : " + nextCommittedIndex);

            if (shouldCommitted(nextCommittedIndex)) {
                commitLogEntry(nextCommittedIndex);
            }
        }
        else if (term == currentTerm && !success) {

            // Update nextIndexMap for next round sendAppendEntries()

            // If AppendEntries fails because of log inconsistency, decrement nextIndex and retry

            nextIndexMap.put(followerId, nextIndexMap.get(followerId) - 1);
        }
    }

    private boolean shouldCommitted(int nextCommittedIndex) {

        // Mark log entries committed if stored on a majority of servers and
        // at least one entry from current term is stored on a majority of servers

        int numOfFollowerAppend = 0;

        for (Map.Entry<String, Integer> entry : matchIndexMap.entrySet()) {
            if (entry.getValue() >= nextCommittedIndex) {
                numOfFollowerAppend++;
            }
        }

        return numOfFollowerAppend >= majority
                && logEntries.get(nextCommittedIndex - 1).term == currentTerm;

    }


    private void commitLogEntry(int nextCommittedIndex) {

        // Previous log entries can be possibly not committed due to no entry in current term has committed
        // As long as next Committed Index is about to be committed, all uncommitted log entries before it will be committed now.

        for (int i = committedIndex + 1; i <= nextCommittedIndex; i++) {

            //Before accepting command, leader checks its log for entry with that id
            LogEntry currentLogEntry = logEntries.get(i - 1);
            boolean isCommitted = false;

            for (int j = 0; j < committedIndex; j++) {
                LogEntry previousEntry = logEntries.get(j);

                if (previousEntry.equals(currentLogEntry)) {

                    isCommitted = true;
                    break;
                }
            }

            if (!isCommitted) {
                // Print in the corresponding log
                try {
                    changeProperty("log_" + dataCenterId, "Committed Log Entry_" + committedEntryCounter,
                            "RequestId " + currentLogEntry.requestId + " : " + currentLogEntry.clientId +
                                    " successfully bought " + currentLogEntry.numOfTicket + " tickets.");

                    System.out.println("Increment committedIndex ..");
                    committedIndex++;

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // if leader, substract from TotalNumOfTicket

                if (this.currentRole == Role.Leader) {
                    try {
                        int globalNumOfTicket = Integer.parseInt(readConfig("Config_" + dataCenterId, "GlobalTicketNumber"));
                        changeProperty("Config_" + dataCenterId, "GlobalTicketNumber",
                                String.valueOf(globalNumOfTicket - currentLogEntry.numOfTicket));

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }

                    // if leader, notify client with successful information

                    try {
                        Registry registry = LocateRegistry.getRegistry("127.0.0.1", currentLogEntry.clientPort);
                        Client_Comm client = (Client_Comm) registry.lookup(currentLogEntry.clientId);
                        if (client != null) {
                            client.responseToRequest(true);
                        }
                        System.out.println("");

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }

            }
            else if (isCommitted){

                // if leader, notify client with successful information
                System.out.println("Calling Client ...");
                try {
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1", currentLogEntry.clientPort);
                    Client_Comm client = (Client_Comm) registry.lookup(currentLogEntry.clientId);
                    if (client != null) {
                        client.responseToRequest(true);
                    }
                    System.out.println("");

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    private void reply(boolean isSuccess, int matchIndex, int leaderPort, String leaderId){
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", leaderPort);
            DC_Comm comm = (DC_Comm) registry.lookup(leaderId);
            if (comm != null) {
                comm.handleReply(currentTerm, isSuccess, matchIndex, dataCenterId);
            }
            System.out.println("Reply with " + isSuccess + " ACK...");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void sendVote(boolean isVote, int myPort, String candidateId){
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", myPort);
            DC_Comm comm = (DC_Comm) registry.lookup(candidateId);
            if (comm != null) {
                comm.handleVote(currentTerm, isVote, dataCenterId);
            }
            System.out.println("Reply with " + isVote + " vote...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void updateTerm(int term) {
        currentTerm = term;
        System.out.println("Current Term has been updated to " + currentTerm + " ...");
    }

    private void convertRole(Role laterRole){
        currentRole = laterRole;
        System.out.println("Current Role has been converted to " + currentRole + " ...");
    }



    private void startTimer(){
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                initiateElection();
            }
        };

        timer.schedule(timerTask, ELECTION_INTERVAL);
        System.out.println(ELECTION_INTERVAL + " milliseconds Timer is set ...");
    }

    private void resetTimer(){

        timer.cancel();
        timer.purge();
        startTimer();
    }

    //Become Candidate
    private void initiateElection(){
        if (this.currentRole != Role.Leader) {
            //Convert to candidate role at the beginning
            System.out.println("New Election starts!");
            this.convertRole(Role.Candidate);
            this.updateTerm(this.currentTerm + 1);
            voteFor = dataCenterId;
            resetTimer();
            try {
                broadcastRequestVote();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }


    //Become Follower
    private void becomeFollower(){
        System.out.println("Step down as a follower ...");
        convertRole(Role.Follower);
        resetTimer(); // If not receive heart beat for certain time, start a new election.
    }


    //Become Leader
    private void becomeLeader(){
        System.out.println("Step up as a leader ...");
        convertRole(Role.Leader);
        try{
            changeProperty("Leader", "CurrentLeader", dataCenterId);
        } catch (Exception ex){
            ex.printStackTrace();
        }


        // initialize nextIndex for each follower to lastLogIndex + 1

        for (Map.Entry<String, Integer> entry : nextIndexMap.entrySet()) {
            entry.setValue(lastLogIndex + 1);
        }

        // Broadcast AppendEntries as normal.
        // The logic of repairing log and append entry will be included in the sendAppendEntries()

        while(this.currentRole == Role.Leader) {
            try {
                //Send out Heart Beat
                Thread.sleep(HEARTBEAT_INTERVAL);
                broadcastAppendEntries();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }




    // Create Socket Server listen to heartbeat.
    // If no heartbeat sent in certain time, initiate election.

    private void initialize() {
        System.out.println("Initializing Data Center " + dataCenterId + " ...");
        Registry reg = null;
        try {
            reg = LocateRegistry.createRegistry(this.port);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }
        System.out.println("Data Center " + dataCenterId + " is Waiting...");
        try {
            if (reg != null) {
                reg.rebind(this.dataCenterId, this); //Listening to RMI call from other data center
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }

        startTimer();

    }

    private void broadcastAppendEntries() throws IOException{
        System.out.println("Broadcasting heartbeat to all data centers...");
        int totalNumOfDataCenter = Integer.parseInt(readConfig("Config_" + dataCenterId,"TotalNumOfDataCenter"));
        for (int id = 1; totalNumOfDataCenter != 0; id++, totalNumOfDataCenter--){
            int port = Integer.parseInt(readConfig("Config_" + dataCenterId, "D" + id + "_PORT"));
            if (port != this.port) {
                sendAppendEntries(id, port);
            }
        }
        showLogEntries();
    }

    private void sendAppendEntries(int id, int port) throws IOException {

        String peerId = "D" + id;

        int nextIndex = nextIndexMap.get(peerId);

        LogEntry entry = null;

        int prevIndex = lastLogIndex;
        int prevTerm = lastLogTerm;
        int prevPrevTerm = lastLogTerm;

        if (nextIndex <= lastLogIndex) {
            entry = logEntries.get(nextIndex - 1);
            prevIndex = nextIndex;
            prevTerm = logEntries.get(nextIndex - 1).term;

        }
        if (nextIndex >= 2) {
            prevPrevTerm = logEntries.get(nextIndex - 2).term;
        }

        System.out.println("nextIndex: " + nextIndex);
        System.out.println("lastLogIndex: " + lastLogIndex);
        System.out.println("lastLogTerm: " + lastLogTerm);
        System.out.println("prevIndex: " + prevIndex);
        System.out.println("prevTerm: " + prevTerm);
        System.out.println("prevPrevTerm: " + prevPrevTerm);
        if (entry != null) {
            System.out.println("entry" + entry.toString());
        } else {
            System.out.println("entry is null");
        }

        AppendEntries appendEntries = new AppendEntries(currentTerm, dataCenterId, prevIndex,
                                                        prevTerm, prevPrevTerm, entry, committedIndex, this.port);

        try{
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);
            DC_Comm dc = (DC_Comm) registry.lookup("D" + id);
            if (dc != null) {
                dc.handleAppendEntries(appendEntries);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void broadcastRequestVote() throws IOException {
        System.out.println("Broadcasting RequestVote to all data centers...");
        int totalNumOfDataCenter = Integer.parseInt(readConfig("Config_" + dataCenterId,"TotalNumOfDataCenter"));
        for (int id = 1; totalNumOfDataCenter != 0; id++, totalNumOfDataCenter--){
            int port = Integer.parseInt(readConfig("Config_" + dataCenterId, "D" + id + "_PORT"));
            if (port != this.port) {
                try{
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);

                    // An interface can be returned, though it has to be implemented somewhere in the program.
                    // In this case, DC_Comm is a Data center, because Data Center implements DC_Comm.

                    DC_Comm dc = (DC_Comm) registry.lookup("D" + id);
                    if (dc != null) {
                        dc.handleRequestVote(dataCenterId, currentTerm, lastLogIndex, lastLogTerm, this.port);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void showLogEntries() {
        System.out.println("Following are entries in my Log : ");
        System.out.println(" ");
        for(LogEntry entry : logEntries) {
            System.out.println("index: " + entry.index);
            System.out.println("term: " + entry.term);
            System.out.println("numOfTicket: " + entry.numOfTicket);
            System.out.println("clientId: " + entry.clientId);
            System.out.println("requestId: " + entry.requestId);
            System.out.println("clientPort: " + entry.clientPort);
            System.out.println("isConfigChange: " + entry.isConfigChange);
        }
    }

    public static void main (String[] args) {


        try {
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter Data Center ID: ");
            String dataCenterId = scan.nextLine().trim();
            System.out.println("Enter Socket Port: ");
            int port = Integer.parseInt(scan.nextLine().trim());
            String isReady = "N";
            while(isReady.equals("N")) {
                System.out.println("Ready to Start the Data Center (Y / N): ");
                isReady = scan.nextLine().trim();
            }
            DataCenter server = new DataCenter(dataCenterId, port);
            changeProperty("Config_" + dataCenterId, dataCenterId + "_PORT", String.valueOf(port));
            server.initialize();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
