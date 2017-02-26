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

    public RequestVote(String dataCenterId, int term, int lastLogIndex, int lastLogTerm){
        this.candidateId = dataCenterId;
        this.term = term;
        this.lastLogIndex = lastLogIndex;
        this.lastLogTerm = lastLogTerm;
    }

}
