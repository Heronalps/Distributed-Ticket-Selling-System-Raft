package com.ucsb.michaelzhang;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import static com.ucsb.michaelzhang.Configuration.*;


public class Main {



    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1102);
            DC_Comm comm = (DC_Comm) registry.lookup("D2");
            if (comm != null) {
                comm.handleVote(2, true, "D3");
            }
            System.out.println("Reply with " + true + " vote to " + "D2" + " ...");
        } catch (NotBoundException | RemoteException ex) {
            ex.printStackTrace();
        }
    }
}
