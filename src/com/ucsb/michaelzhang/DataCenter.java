package com.ucsb.michaelzhang;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static com.ucsb.michaelzhang.Configuration.changeProperty;
import static com.ucsb.michaelzhang.Configuration.readConfig;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class DataCenter extends UnicastRemoteObject implements DC_Comm {

    static final int HEARTBEAT_INTERVAL = 3000;
    static final int ELECTION_INTERVAL = 10 * 60 * 1000;
    int currentTerm;
    String voteFor;
    ArrayList<LogEntry> logEntries; //logEntries is 1-based, instead of zero-based
    Role currentRole;
    String dataCenterId; //D1, D2, D3 ...
    Timer timer;
    int port;
    int numOfVotes;
    int majority; //Current majority of network
    int lastLogIndex;
    int lastLogTerm;


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
        try{
            int total = Integer.parseInt(readConfig("Config", "TotalNumOfDataCenter"));
            majority = total / 2 + 1;
        } catch (IOException ex){
            ex.printStackTrace();
        }


    }

    // Client DC Communication
    public void request(int numOfTicket, int requestId) throws RemoteException {

        //call responseToRequest()
    }


    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.

    public void show() throws RemoteException{

    }


    //Config change command. Parameter list will be modified later.
    public void change() throws RemoteException{

    }



    //Inter-DataCenter Communication
    public void sendRequestVote(String candidateId, //Sender's data center ID
                                int term,
                                int lastLogIndex,
                                int lastLogTerm,
                                int myPort) throws RemoteException{

        System.out.println("Received RequestVote from " + candidateId);
        //sendVote()
        if (term < currentTerm) {
            vote(false, myPort, candidateId);

        } else if(term > currentTerm) {
            updateTerm(term);
            if(this.currentRole == Role.Candidate || this.currentRole == Role.Leader) {
                becomeFollower();
            }
            if (lastLogIndex >= this.lastLogIndex){
                voteFor = candidateId;
                vote(true, myPort, candidateId);

                resetTimer(); //Only reset timer when I vote true.

            } else {
                voteFor = null;
                vote(false, myPort, candidateId);
            }
        } else {
            if ((voteFor == null || voteFor == candidateId) && lastLogIndex >= this.lastLogIndex ) {
                vote(true, myPort, candidateId);
                resetTimer();
            }
        }
    }

    public void sendVote(int term,
                         boolean voteGranted) throws RemoteException {

        if (term <= currentTerm) {
            if (voteGranted) {
                numOfVotes++;
            }
            if (numOfVotes >= majority) {
                becomeLeader();
            }
        } else {
            updateTerm(term);
            becomeFollower();
        }
    }

    public void sendAppendEntries(int term,
                                  String leadId,
                                  int prevLogIndex,
                                  int prevLogTerm,
                                  LogEntry entries, //log entries to store, empty for heartbeat
                                  int commitIndex,
                                  int myPort) throws RemoteException {

        if (term > currentTerm && (this.currentRole == Role.Candidate || this.currentRole == Role.Leader)) {
            updateTerm(term);
            becomeFollower();
            reply(true, myPort, leadId);
        } else if (term == currentTerm) {

            //Reset the Timer whenever an AppendEntries received
            resetTimer();

            //sendACK()
            //if (entries == null)... HeartBeat
            //else ... Request
        }
    }

    //Followers' Reply to AppendEntries
    public void sendReply(int term,
                        boolean success) throws RemoteException{

    }






    private void reply(boolean isSuccess, int myPort, String leaderId){
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", myPort);
            DC_Comm comm = (DC_Comm) registry.lookup(leaderId);
            if (comm != null) {
                comm.sendReply(currentTerm, isSuccess);
            }
            System.out.println("Reply with " + isSuccess + " ACK...");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void vote(boolean isVote, int myPort, String candidateId){
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", myPort);
            DC_Comm comm = (DC_Comm) registry.lookup(candidateId);
            if (comm != null) {
                comm.sendVote(currentTerm, isVote);
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
        System.out.println(ELECTION_INTERVAL + " milliseconds Timer is reset ...");
    }

    private void resetTimer(){

        timer.cancel();
        timer.purge();
        startTimer();
        System.out.println(ELECTION_INTERVAL + " milliseconds Timer is reset ...");
    }

    //Become Candidate
    private void initiateElection(){

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
            changeProperty("Config", "CurrentLeader", dataCenterId);
        } catch (Exception ex){
            ex.printStackTrace();
        }


        // Repair followers' logs
        //TODO



        while(this.currentRole == Role.Leader) {
            try {
                //Send out Heart Beat
                Thread.sleep(HEARTBEAT_INTERVAL);
                if(this.logEntries.size() == lastLogIndex) {
                    broadcastAppendEntries(null);
                }

                //Send out Request
                //TODO

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

    public void broadcastAppendEntries(LogEntry logEntry) throws IOException{
        System.out.println("Broadcasting heartbeat to all data centers...");
        int totalNumOfDataCenter = Integer.parseInt(readConfig("Config","TotalNumOfDataCenter"));
        for (int id = 1; totalNumOfDataCenter != 0; id++, totalNumOfDataCenter--){
            int port = Integer.parseInt(readConfig("Config", "D" + id + "_PORT"));
            if (port != this.port) {
                try{
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);
                    DC_Comm comm = (DC_Comm) registry.lookup("D" + id);
                    if (comm != null) {
                        comm.sendAppendEntries(currentTerm, dataCenterId, lastLogIndex, lastLogTerm, logEntry, lastLogIndex, this.port);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void broadcastRequestVote() throws IOException {
        System.out.println("Broadcasting RequestVote to all data centers...");
        int totalNumOfDataCenter = Integer.parseInt(readConfig("Config","TotalNumOfDataCenter"));
        for (int id = 1; totalNumOfDataCenter != 0; id++, totalNumOfDataCenter--){
            int port = Integer.parseInt(readConfig("Config", "D" + id + "_PORT"));
            if (port != this.port) {
                try{
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1", port);
                    DC_Comm comm = (DC_Comm) registry.lookup("D" + id);
                    if (comm != null) {
                        comm.sendRequestVote(dataCenterId, currentTerm, lastLogIndex, lastLogTerm, this.port);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }



    public static void main (String[] args) {


        try {
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter Data Center ID: ");
            String dataCenterId = scan.nextLine().trim();
            System.out.println("Enter Socket Port: ");
            int port = Integer.parseInt(scan.nextLine().trim());
            DataCenter server = new DataCenter(dataCenterId, port);
            changeProperty("Config", dataCenterId + "_PORT", String.valueOf(port));
            server.initialize();

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
