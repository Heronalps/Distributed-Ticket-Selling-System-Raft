package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 2/23/17.
 */
public class ConfigChange extends LogEntry {

    public static <E> void  printArray(E[] inputArray) {
        // Display array elements
        for (E element : inputArray) {
            System.out.printf("%s ", element);
        }
        System.out.println();

    }
}
