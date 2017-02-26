package com.ucsb.michaelzhang;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

/**
 * Created by michaelzhang on 2/25/17.
 */
public class Test {

    public static void main(String[] args) {
        ArrayList<Integer> array = new ArrayList<>();
        //array.add(1);
        System.out.println(array.size());
        System.out.println(array.get(0));

    }
}
