package com.ucsb.michaelzhang;

import java.io.IOException;

import static com.ucsb.michaelzhang.Configuration.*;


public class Main {



    public static void main(String[] args) throws IOException{

        String str = readConfig("Config", "CurrentLeader");
        System.out.println(str);
    }
}
