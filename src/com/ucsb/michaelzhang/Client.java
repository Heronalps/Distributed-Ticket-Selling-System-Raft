package com.ucsb.michaelzhang;

import sun.rmi.runtime.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import static com.ucsb.michaelzhang.Configuration.*;

/**
 * Created by michaelzhang on 2/22/17.
 * The client side App of Raft
 */
public class Client extends UnicastRemoteObject implements Client_Comm {

    private static final int TIMEOUT = 10 * 1000;

    // Leader ID of current leader to connect. Null if unknown.
    private String dataCenterId;
    private int dataCenterPort;
    private Timer timer;
    private String clientId;
    private int port;
    private int requestId; // To avoid execute the same order multiple times. Only if the request fulfilled, the requestId increments.
    private int numOfTicket;
    private int counter; // How many times does client send the same request. Reset to one after a request fulfilled.
    private HashMap<String, Integer> dataCenterMap;

    private Client(String clientId, int port) throws RemoteException {

        this.clientId = clientId;
        this.port = port;
        this.requestId = 1;
        this.counter = 1;
        this.dataCenterId = "D" + clientId.substring(1);
        this.dataCenterMap = new HashMap<>();

        try{
            dataCenterPort = Integer.parseInt(readConfig("Config_" + dataCenterId, dataCenterId + "_PORT"));

            int totalNumOfCurrentDataCenter
                    = Integer.parseInt(readConfig("Config_D" + clientId.substring(1), "TotalNumOfDataCenter"));
            for (int j = 1; j <= totalNumOfCurrentDataCenter; j++) {
                int dcPort = Integer.parseInt(readConfig("Config_D" + clientId.substring(1), "D" + j + "_PORT"));
                dataCenterMap.put("D" + j, dcPort);

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

        timer.scheduleAtFixedRate(timerTask, 0, TIMEOUT);
        System.out.println("");
        System.out.println(TIMEOUT + " milliseconds Timer starts ...");
    }

    private void cancelTimer(){

        timer.cancel();
        timer.purge();
    }

    private void sendClientRequest(){

        // Only if leader crashes, the new request from client would be effective to reach to new leader.
        // So, every time resend request, client needs to pull out possibly new Leader information.

        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", dataCenterPort);
            DC_Comm dc = (DC_Comm) registry.lookup(dataCenterId);
            System.out.println("Send request to Data Center " + dataCenterId + " to buy " + numOfTicket + " tickets for the "
                    + counter + " time ...");
            counter++;

            dc.handleRequest(numOfTicket, clientId, requestId, this.port);

        } catch (NotBoundException | RemoteException ex) {
            System.out.println(dataCenterId + " is not responding to ticket request...");
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

        cancelTimer();
        counter = 1;
        System.out.println("Received a response from " + dataCenterId + " ...");
        if (success){
            System.out.println("Successfully bought " + numOfTicket + " tickets ... ");

            // Only when request is successfully fulfilled, the requestId will increment.

            this.requestId++;
            this.counter = 1;

        } else {
            System.out.println("There's no sufficient tickets left in the pool ...");
        }
        showMenu();
    }

    private void sendConfigChange(boolean upOrDown,
                                  HashMap<String, Integer> oldDataCenterMap, HashMap<String, Integer> newDataCenterMap) {

        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", dataCenterPort);
            DC_Comm dc = (DC_Comm) registry.lookup(dataCenterId);
            dc.handleConfigChange(upOrDown, true, oldDataCenterMap, newDataCenterMap, clientId, port);

        } catch (NotBoundException | RemoteException ex) {
            System.out.println(dataCenterId + " is not responding to Configuration change request...");
        }
    }

    public void responseToChange() throws RemoteException{
        System.out.println("The new Configuration has been committed to data centers ... ");
        showMenu();
    }


    private void buy(int numOfTicket) throws InterruptedException{
        if (numOfTicket > 0) {
            this.numOfTicket = numOfTicket;
            startTimer();
        } else {
            System.out.println("Please enter a valid ticket number again ...");
        }
    }

    //First line shows the state of the state machine for the application.
    //Following lines show committed log of the datacenter connected to.

    // Call data center's showLogEntries and retrun entry.toString();

    private void show(){

        // First line show ticket left in the pool

        // Following lines show the connected data center's log file
        try {
            int globalNumOfTicket = Integer.parseInt(readConfig("Config_" + dataCenterId, "GlobalTicketNumber"));
            System.out.println("There are " + globalNumOfTicket + " left in the pool ...");
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", dataCenterPort);
            DC_Comm dc = (DC_Comm) registry.lookup(dataCenterId);
            ArrayList<LogEntry> list = dc.fetchCommittedLogEntries();
            for (int i = 0; i < list.size(); i++) {
                System.out.println( "[" + i + "] : "+ list.get(i).toString());
            }

        } catch (NotBoundException | IOException ex) {
            System.out.println("Can't fetch committed log entries from " + dataCenterId);
        }

    }


    private static void showMenu(){
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
            showMenu();

            while(true) {

                String[] command = scan.nextLine().split(" ");
                switch (command[0]) {
                    case "buy":

                        client.buy(Integer.parseInt(command[1]));
                        break;
                    case "show":

                        client.show();
                        break;
                    case "change":

                        if (!command[1].equals("-up") && !command[1].equals("-down")) {
                            System.out.println("Invalid argument! Please enter command again ... ");
                        } else {
                            int numOfDataCenter = (command.length - 2) / 2;

                            // dataCenterMap has all data center's information

                            HashMap<String, Integer> newDataCenterMap = new HashMap<>();

                            for (Map.Entry<String, Integer> entry : client.dataCenterMap.entrySet()) {
                                newDataCenterMap.put(entry.getKey(), entry.getValue());
                            }
                            if (command[1].equals("-up")) {
                                int i = 2;
                                int counter = 1;
                                while (counter <= numOfDataCenter) {
                                    newDataCenterMap.put(command[i], Integer.parseInt(command[i + 1]));
                                    i += 2;
                                    counter++;
                                }
                            } else {
                                int i = 2;
                                int counter = 1;
                                while (counter <= numOfDataCenter) {
                                    newDataCenterMap.remove(command[i]);
                                    i += 2;
                                    counter++;
                                }
                            }


                            // true represents up, whereas false represents down

                            boolean upOrDown = command[1].equals("-up");
                            client.sendConfigChange(upOrDown, client.dataCenterMap, newDataCenterMap);

                            // Update client dataCenterMap
                            if (newDataCenterMap.size() > client.dataCenterMap.size()) {
                                for (Map.Entry<String, Integer> entry : newDataCenterMap.entrySet()) {
                                    if (!client.dataCenterMap.containsKey(entry.getKey())) {
                                        client.dataCenterMap.put(entry.getKey(), entry.getValue());
                                    }
                                }
                            } else if (newDataCenterMap.size() < client.dataCenterMap.size()) {
                                for (Map.Entry<String, Integer> entry : client.dataCenterMap.entrySet()) {
                                    if (!newDataCenterMap.containsKey(entry.getKey())) {
                                        client.dataCenterMap.remove(entry.getKey());
                                    }
                                }
                            }

                        }
                        break;
                }
            }
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
