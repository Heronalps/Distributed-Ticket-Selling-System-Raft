package com.ucsb.michaelzhang;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import static com.ucsb.michaelzhang.Configuration.*;


public class Main {



    public static void main(String[] args) {
        try {
            duplicateFile("Config", "Config_D1");
            duplicateFile("Config", "Config_D2");
            duplicateFile("Config", "Config_D3");
            duplicateFile("Config_Original", "Config_D4");
            duplicateFile("Config_Original", "Config_D5");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
