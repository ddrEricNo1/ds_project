package com.ds.role.remoteClient;

import com.ds.pojo.Item;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.List;

public interface RemoteClient extends Remote {
    // get the bidding price of the current item
    float getCurrentBiddingPrice() throws RemoteException;

    // to display the information of the current bidding item
    Item getItemDetails() throws RemoteException;

    // update the bidding price of the current item
    boolean updateBiddingPrice(String contact_info, float biddingPrice) throws IOException, NotBoundException;

    // get the highest bidder of the current item
    String getHighestBidder() throws RemoteException;

    // authenticate the bidder
    boolean authenticateUser(String email, String password) throws RemoteException, UnsupportedEncodingException, NoSuchAlgorithmException, SQLException, MalformedURLException, NotBoundException;

    // register a new bidder
    boolean registerUser(String email, String password) throws RemoteException, SQLException, UnsupportedEncodingException, NoSuchAlgorithmException;

    void addClient(String contact_info) throws RemoteException, MalformedURLException, NotBoundException;

    List<Item> getAllItems() throws RemoteException;

    Item getItemAt(int selectedIndex) throws RemoteException;

    boolean logOut(String contact_info) throws RemoteException;
}
