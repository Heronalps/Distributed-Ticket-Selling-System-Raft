package com.ucsb.michaelzhang;

/**
 * Created by michaelzhang on 2/25/17.
 */
import java.rmi.*;

public interface RMIInterface extends Remote {

    void sendMessage(String text) throws RemoteException;

    String getMessage(String text) throws RemoteException;

}
