package com.ds.Main;

import com.ds.UI.Login;
import com.ds.UI.BidderUI;
import com.ds.role.remoteClient.RemoteClient;
import com.ds.pojo.Item;
import com.ds.pojo.User;
import com.ds.role.remoteServer.RemoteServer;

import javax.swing.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;

public class Client extends UnicastRemoteObject implements RemoteServer {
    // a reference pointing to the current frame page
    private JFrame currentPage;

    // remote client
    private static RemoteClient rc;

    static {
        try {
            rc = (RemoteClient) LocateRegistry.getRegistry("10.71.107.183").lookup("remoteClient");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        } catch (NotBoundException e) {
            throw new RuntimeException(e);
        }
    }

    public Client() throws RemoteException {
        super();
        this.currentPage = new Login(rc, this);
    }

    // change the current frame of the client
    public void changeFrame(JFrame nextFrame) {
        this.currentPage = nextFrame;
    }

    // main function
    public static void main(String[] args) throws RemoteException {
        Client client = new Client();
    }

    public double getCurrentBid() {
        try {
            return rc.getCurrentBiddingPrice();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public Item getItemDetails() {
        try {
            return rc.getItemDetails();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getHighestBidder() {
        try {
            return rc.getHighestBidder();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    this is the client RMI method, which will be used by the server to send messages to the client
     */
    @Override
    public void messageFromServer(String msg) throws RemoteException {
        System.out.println("message: " + msg);
        ((BidderUI)(this.currentPage)).showServerMessage(msg);
    }
}
