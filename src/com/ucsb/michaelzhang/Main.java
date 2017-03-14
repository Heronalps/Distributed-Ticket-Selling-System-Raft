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
            deleteProperty("log_D1", "Committed Log Entry_1");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
