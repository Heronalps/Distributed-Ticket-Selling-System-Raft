package com.ucsb.michaelzhang;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by michaelzhang on 3/10/17.
 */
public class Test {
    static Map<String, Integer> map = new HashMap<>();

    public Test(String id){
        map.put(id, 0);
    }

    public static void main(String[] args) {
        Test a = new Test("D1");
        Test b = new Test("D2");
        System.out.println("");

    }
}
