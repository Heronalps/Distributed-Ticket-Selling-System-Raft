package com.ucsb.michaelzhang;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by michaelzhang on 2/26/17.
 */
public interface Client_Comm extends Remote {

    // Client DC_Comm
    void responseToRequest(boolean success) throws RemoteException;


    void responseToChange() throws RemoteException;
    //Configuration change command. Parameter list will be modified later.
}
