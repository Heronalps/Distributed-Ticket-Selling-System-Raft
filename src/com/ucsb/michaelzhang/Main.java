package com.ucsb.michaelzhang;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;

import static com.ucsb.michaelzhang.Configuration.*;


public class Main {



    public static void main(String[] args) {

        try {
            Registry registry = LocateRegistry.getRegistry("127.0.0.1", 1201);
            Client_Comm client = (Client_Comm) registry.lookup("C1");
            if (client != null) {
                client.responseToRequest(true);
            }
            System.out.println("");

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
