package com.ucsb.michaelzhang;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class DataCenter{

    int currentTerm;
    String voteFor;
    LogEntry[] logEntries;
    Role currentRole;
    String dataCenterId;
    String candidateId;
    String leaderId;
    double timeout;
    Timer timer;
    int port;
    int numOfVotes;


    public enum Role{
        Follower, Candidate, Leader
    }

    public DataCenter(){
        currentRole = Role.Follower;

    }

    public void updateTerm(int term) {
        currentTerm = term;
    }

    public void convertRole(Role laterRole){
        this.currentRole = laterRole;
    }

    public void resetTimer(){
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                initiateElection();
            }
        };
        timer.cancel();
        timer.purge();
        timer.schedule(timerTask, 1000);
    }

    public boolean initiateElection(){

        //Convert to candidate role at the beginning
        this.convertRole(Role.Candidate);
        this.updateTerm(this.currentTerm + 1);


        return false;
    }

    public void initialize() throws IOException, ClassCastException{
        // Create Socket Server listen to heartbeat.
        // If no heartbeat sent in certain time, initiate election.

        ServerSocket serverSocket = new ServerSocket();
        serverSocket.setReuseAddress(true);
        serverSocket.bind(new InetSocketAddress(this.port));
        try {
            long start = System.currentTimeMillis();
            long end = start + 3 * 60 * 1000; // Process will be closed after 3 minutes

            resetTimer();

            while (System.currentTimeMillis() < end) {
                Socket clientSocket = serverSocket.accept();
                ObjectInputStream inFromClient =
                        new ObjectInputStream(clientSocket.getInputStream());
                try {
                    //System.out.println(this.dataCenterID + ": A Message Received ...");

                    //Delay for 2 second on every message received
                    //Thread.sleep(2000);
                    Message msg = (Message) inFromClient.readObject();



                    // HeartBeat

                    if (msg instanceof AppendEntries) {
                        resetTimer();
                    }

                    //RequestVote

                    if (msg instanceof RequestVote) {

                    }

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } finally {
                    inFromClient.close();
                    clientSocket.close();
                }
            }
        } finally {
            serverSocket.close();
        }
    }

    public void grantVote(){
        resetTimer();
    }

    public void broadcastHeartBeat(){

    }



    public static void main (String[] args) throws IOException{
        DataCenter dataCenter = new DataCenter();
        dataCenter.initialize();
    }
}