package com.ucsb.michaelzhang;

import java.io.Serializable;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class Vote extends Message implements Serializable{
    int term;
    boolean voteGranted;
}
