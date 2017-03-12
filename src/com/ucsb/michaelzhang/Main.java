package com.ucsb.michaelzhang;

import java.io.IOException;
import java.util.Scanner;

import static com.ucsb.michaelzhang.Configuration.*;


public class Main {



    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);
        System.out.println("Please enter your command (buy / show / change) : ");
        String[] command = scan.nextLine().split(" ");
        System.out.println("");

    }
}
