package com.ucsb.michaelzhang;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static com.ucsb.michaelzhang.Configuration.*;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Client implements Client_Comm {

    static final int TIMEOUT = 6000;

    // Leader ID of current leader to connect. Null if unknown.
    String currentLeaderId;
    int currentLeaderPort;
    Timer timer;
    String clientId;
    int port;
    int requestId; // To avoid execute the same order multiple times
    int numOfTicket;
    boolean isSuccess;

    private Client(String clientId, int port) {

        this.clientId = clientId;
        this.port = port;
        this.requestId = 0;
        this.isSuccess = false;

        try{
            currentLeaderId = readConfig("Config", "CurrentLeader");
            if (currentLeaderId != null){
                currentLeaderPort = Integer.parseInt(readConfig("Config", currentLeaderId + "_PORT"));
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
        if (currentLeaderId != null) {
            try {
                Registry registry = LocateRegistry.getRegistry("127.0.0.1", currentLeaderPort);
                DC_Comm comm = (DC_Comm) registry.lookup(currentLeaderId);
                if (comm != null) {
                    comm.request(numOfTicket, requestId);
                    requestId++;
                }
                System.out.println("Send request to Data Center " + currentLeaderId + " to buy " + numOfTicket + " tickets for the "
                        + requestId + " time ...");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void initialize(){
        System.out.println("Initializing Data Center " + clientId + " ...");
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




    public void responseToRequest(boolean success){
        if (success){
            this.isSuccess = true;
            cancelTimer();
        }
    }

    public void responseToShow(){

    }

    public void responseToChange() {

    }


    public void buy(int numOfTicket){
        this.numOfTicket = numOfTicket;
        sendClientRequest();
        startTimer();
    }

    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.
    public void show(){

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
            changeProperty("Config", clientId + "_PORT", String.valueOf(port));
            client.initialize();

            while(true) {
                System.out.println("Enter Ticket Number: ");
                int numOfTicket = Integer.parseInt(scan.nextLine().trim());
                client.buy(numOfTicket);
                while (client.isSuccess == false) {
                    Thread.sleep(TIMEOUT);
                    System.out.println("The request of " + numOfTicket + " hasn't been fulfilled yet ...");
                }
                System.out.println("Successfully bought " + numOfTicket + " tickets ... ");
                client.show();
            }



        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
