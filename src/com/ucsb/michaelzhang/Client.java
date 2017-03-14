package com.ucsb.michaelzhang;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static com.ucsb.michaelzhang.Configuration.*;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Client extends UnicastRemoteObject implements Client_Comm {

    static final int TIMEOUT = 1 * 5 * 1000;

    // Leader ID of current leader to connect. Null if unknown.
    String currentLeaderId;
    int currentLeaderPort;
    Timer timer;
    String clientId;
    int port;
    int requestId; // To avoid execute the same order multiple times. Only if the request fulfilled, the requestId increments.
    int numOfTicket;
    boolean isSuccess;
    int counter; // How many times does client send the same request. Reset to one after a request fulfilled.

    private Client(String clientId, int port) throws RemoteException {

        this.clientId = clientId;
        this.port = port;
        this.requestId = 1;
        this.isSuccess = false;
        this.counter = 1;

        try{
            currentLeaderId = readConfig("Leader", "CurrentLeader");
            if (currentLeaderId != null){
                currentLeaderPort = Integer.parseInt(readConfig("Config_D" + clientId.substring(1), currentLeaderId + "_PORT"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void startTimer(){
        this.timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                sendClientRequest();
            }
        };

        timer.scheduleAtFixedRate(timerTask, TIMEOUT, TIMEOUT);
        System.out.println(TIMEOUT + " milliseconds Timer starts ...");
    }

    private void cancelTimer(){

        timer.cancel();
        timer.purge();
    }

    private void sendClientRequest(){

        // Only if leader crashes, the new request from client would be effective to reach to new leader.
        // So, every time resend request, client needs to pull out possibly new Leader information.

        try{
            currentLeaderId = readConfig("Leader", "CurrentLeader");
            if (currentLeaderId != null){
                currentLeaderPort = Integer.parseInt(readConfig("Config_D" + clientId.substring(1), currentLeaderId + "_PORT"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (currentLeaderId != null) {
            try {
                Registry registry = LocateRegistry.getRegistry("127.0.0.1", currentLeaderPort);
                DC_Comm dc = (DC_Comm) registry.lookup(currentLeaderId);
                if (dc != null) {
                    dc.handleRequest(numOfTicket, clientId, requestId, this.port, false);
                }
                System.out.println("Send request to Data Center " + currentLeaderId + " to buy " + numOfTicket + " tickets for the "
                        + counter + " time ...");
                counter++;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void initialize(){
        System.out.println("Initializing Client " + clientId + " ...");
        Registry reg = null;
        try {
            reg = LocateRegistry.createRegistry(this.port);
        } catch (Exception e) {
            System.out.println("ERROR: Could not create the registry.");
            e.printStackTrace();
        }
        System.out.println("Client " + clientId + " is Waiting...");
        try {
            if (reg != null) {
                reg.rebind(this.clientId, this); //Listening to RMI call from data center's response
            }
        } catch (Exception e) {
            System.out.println("ERROR: Failed to register the server object.");
            e.printStackTrace();
        }
    }


    // These three functions are handling the response from the data center

    public void responseToRequest(boolean success) throws RemoteException {

        System.out.println("I am reached ...");
        if (success){
            this.isSuccess = true;
            cancelTimer();
        }
    }

    public void responseToShow(){

    }

    public void responseToChange() {

    }


    public void buy(int numOfTicket) throws InterruptedException{

        try{
            int globalNumOfTicket = Integer.parseInt(readConfig("Config_" + currentLeaderId, "GlobalTicketNumber"));
            if (numOfTicket > globalNumOfTicket) {
                System.out.println("Sorry. There is no sufficient tickets left in the pool.");
                return;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        this.numOfTicket = numOfTicket;
        sendClientRequest();
        startTimer();

        while (!this.isSuccess) {
            Thread.sleep(TIMEOUT);
            System.out.println("The request of " + numOfTicket + " tickets hasn't been fulfilled yet ...");
        }
        System.out.println("Successfully bought " + numOfTicket + " tickets ... ");

        // Only when request is successfully fulfilled, the requestId will increment.

        this.requestId++;
        this.counter = 1;
        this.isSuccess = false;
    }

    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.

    // Call data center's showLogEntries and retrun entry.toString();

    public void show(){

        // First line show the current leader's log file
        try {
            String thisLine;
            String newLeaderId = readConfig("Config", "CurrentLeader");
            String path = "/Users/michaelzhang/Dropbox/Distributed-Ticket-Selling-System-Raft/log_" + newLeaderId;
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((thisLine = bufferedReader.readLine()) != null) {
                System.out.println(thisLine);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Following lines show the connected data center's log file
        try {
            String thisLine;
            String path = "/Users/michaelzhang/Dropbox/Distributed-Ticket-Selling-System-Raft/log_" + currentLeaderId;
            FileReader fileReader = new FileReader(path);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            while ((thisLine = bufferedReader.readLine()) != null) {
                System.out.println(thisLine);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    //Config change command. Parameter list will be modified later.
    public void change() {

    }

    //Parse users' command and call buy() and show();
    public static void main(String[] args) {

        try {
            Scanner scan = new Scanner(System.in);
            System.out.println("Enter Client ID: ");
            String clientId = scan.nextLine().trim();
            System.out.println("Enter Socket Port: ");
            int port = Integer.parseInt(scan.nextLine().trim());

            Client client = new Client(clientId, port);

            //Since request has the information of clientId and port, operation below is unnecessary.
            //changeProperty("Config_D" + clientId.substring(1), clientId + "_PORT", String.valueOf(port));

            client.initialize();

            while(true) {
                System.out.println(" ");
                System.out.println("Command Help:" );
                System.out.println("*********************************************");
                System.out.println("buy [ticket number]");
                System.out.println("*********************************************");
                System.out.println("show");
                System.out.println("*********************************************");
                System.out.println("change -up/-down [Data center ID] [Port] ...");
                System.out.println("*********************************************");
                System.out.println(" ");
                System.out.println("Please enter your command : " );
                System.out.println(" ");

                String[] command = scan.nextLine().split(" ");
                if (command[0].equals("buy")) {

                    client.buy(Integer.parseInt(command[1]));
                }

                else if (command[0].equals("show")) {

                    client.show();
                }

                else if (command[0].equals("change")) {

                    if (command[1].equals("-up")) {

                    }

                    else if (command[1].equals("-down")) {

                    }
                }
            }

        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
