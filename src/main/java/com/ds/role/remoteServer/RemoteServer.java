package com.ds.role.remoteServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServer extends Remote {
    void messageFromServer(String msg) throws RemoteException;
}
