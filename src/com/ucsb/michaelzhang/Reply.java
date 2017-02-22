package com.ucsb.michaelzhang;

import java.io.Serializable;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Reply extends Message implements Serializable{
    //Followers' Reply to AppendEntries
    int term;
    boolean success;
}
