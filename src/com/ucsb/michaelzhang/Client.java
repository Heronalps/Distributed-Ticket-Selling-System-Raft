package com.ucsb.michaelzhang;

import java.util.Timer;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Client {
    // Leader ID of current leader to connect. Null if unknown.
    String currentLeaderId;
    int timeout;
    int numOfTicket;
    Timer timer;

    public boolean buy(int numOfTicket){
        return false;
    }

    public void show(){
        //First line shows the state of the state machine for the application.
        //Following lines show committed log of the datacenter connected to.
    }

    public void change() {
        //Configuration change command. Parameter list will be modified later.
    }

    public static void main(String[] args) {
        //Parse users' command and call buy() and show();
    }
}
