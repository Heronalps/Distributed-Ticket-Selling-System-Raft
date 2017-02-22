package com.ucsb.michaelzhang;

import java.io.Serializable;

/**
 * Created by michaelzhang on 2/22/17.
 */
public class RequestVote extends Message implements Serializable {
    String candidateId;
    int term;
    int lastLogIndex;
    int lastLogTerm;

}
