package com.ucsb.michaelzhang;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by michaelzhang on 2/26/17.
 */
public interface Client_Comm extends Remote {

    // DC's response to Client ticket request
    void responseToRequest(boolean success) throws RemoteException;

    // DC's request to Client configuration change request
    void responseToChange() throws RemoteException;

}
