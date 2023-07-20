package com.ds.Main;

import com.ds.pojo.Item;
import com.ds.role.remoteClient.RemoteClient;
import com.ds.role.remoteClient.RemoteClientImpl;
import com.ds.utils.AuctionTimer;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class Server extends JFrame {
    private final static int timeInterval = 10;

    public static void main(String[] args) throws IOException, URISyntaxException, InterruptedException, AlreadyBoundException {
        // the timer used to count down the remaining time used for the current auction
        AuctionTimer auctionTimer = new AuctionTimer();

        // instantiate an interface
        RemoteClientImpl rc = new RemoteClientImpl(auctionTimer);
        rc.addItem(new Item("mouse", 50, "logitech"));
        rc.addItem(new Item("water", 5, "EastTea"));
        rc.addItem(new Item("laptop", 5000, "DELL"));
        Registry registry = LocateRegistry.createRegistry(1099);
        RemoteClient stub = (RemoteClient) UnicastRemoteObject.exportObject(rc, 8081);
        registry.bind("remoteClient", stub);
    }
}
