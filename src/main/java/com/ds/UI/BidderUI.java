package com.ds.UI;
import com.ds.Main.Client;
import com.ds.role.remoteClient.RemoteClient;import com.ds.pojo.Item;
import java.util.List;
import javax.swing.*;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.rmi.*;
import java.rmi.RemoteException;
public class BidderUI extends JFrame {
    private RemoteClient remoteClient;
    private JList itemList;
    private JList itemDetail;
    private JList serverMessageList;
    private String contact_info;
    private Client client;
    // private JPanel backgroundPanel;
    public DefaultListModel<String> temp;
    public BidderUI(RemoteClient remoteClient, String contact_info, Client client) throws
            RemoteException {
        this.remoteClient = remoteClient;
        this.contact_info = contact_info;
        this.client = client;
        temp = new DefaultListModel<>();
        prepareGUI();
    }

    private void prepareGUI() throws RemoteException{setTitle("Bidder UI");
        setSize(800, 600);
        JLabel headerLabel = new JLabel();
        headerLabel.setText("Auction System");
        headerLabel.setBounds(320, 10, 180, 30);
        headerLabel.setForeground(new Color(0,0,0));
        headerLabel.setFont(new Font("Arial Black", Font.BOLD, 20));
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(50, 60, 325, 200);
        itemList = new JList<>();
        scrollPane.setViewportView(itemList);
        JScrollPane detailScrollPane = new JScrollPane();
        detailScrollPane.setBounds(400, 60, 230, 200);
        itemDetail = new JList<>();
        detailScrollPane.setViewportView(itemDetail);
        JButton backButton = new JButton("Refresh");
        backButton.setBounds(650, 65, 100, 40);
        JButton viewDetailButton = new JButton("View Detail");
        viewDetailButton.setBounds(650, 115, 100, 40);
        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(650, 165, 100, 40);
        JButton logoutButton = new JButton("Log Out");
        logoutButton.setBounds(650, 215, 100, 40);
        JScrollPane messageScrollPane = new JScrollPane();
        messageScrollPane.setBounds(50, 285, 700, 200);
        serverMessageList = new JList<>();messageScrollPane.setName("serverMessage");
        messageScrollPane.setViewportView(serverMessageList);
        JLabel bidAmountLabel = new JLabel();
        bidAmountLabel.setText("Bid Amount: ");
        bidAmountLabel.setBounds(50, 510, 215, 30);
        bidAmountLabel.setForeground(new Color(0,0,0));
        bidAmountLabel.setFont(new Font("Arial Black", Font.BOLD, 16));
        JTextField bidAmountField = new JTextField();
        bidAmountField.setBounds(200, 510, 215, 30);
        JButton bidButton = new JButton("Add Bidding Price");
        bidButton.setBounds(575, 510, 150, 30);
        backButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                itemList.removeAll();
                itemDetail.removeAll();
                bidAmountField.setText("");
                try {
                    showItemList();
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        viewDetailButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = itemList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Item item = null;
                    try {item = remoteClient.getItemAt(selectedIndex);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    showItemDetail(item);
                }
            }
        });
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearItemDetail();
            }
        });
        bidButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = itemList.getSelectedIndex();
                if (selectedIndex != -1) {
                    try {
                        Item item = remoteClient.getItemAt(selectedIndex);
                    } catch (RemoteException ex) {
                        throw new RuntimeException(ex);
                    }
                    String bidAmountStr = bidAmountField.getText();
                    if (!bidAmountStr.isEmpty()) {
                        float bidAmount = Float.parseFloat(bidAmountStr);
                        try {
                            boolean success = remoteClient.updateBiddingPrice(contact_info,
                                    bidAmount);
                            if (success) {
                                showMessage("Bid placed successfully!");
                            } else {showMessage("Failed to place bid. Please enter a higher amount.");
                            }
                        } catch (IOException | NotBoundException ex) {
                            ex.printStackTrace();
                            showMessage("Error occurred while placing bid.");
                        }
                    } else {
                        showMessage("Please enter a bid amount.");
                    }
                }
            }
        });

        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    bidderLogOut(contact_info);
                }catch (RemoteException ex){
                    System.out.println(ex);
                }
            }
        });
        add(scrollPane);
        add(detailScrollPane);
        add(headerLabel);
        add(backButton);
        add(viewDetailButton);
        add(logoutButton);
        add(messageScrollPane);
        add(clearButton);
        add(bidAmountLabel);
        add(bidAmountField);add(bidButton);
// add(backgroundPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setVisible(true);
        showItemList();
        updateServerMessage();
    }
    private void showItemList() throws RemoteException {
        DefaultListModel<String> model = new DefaultListModel<>();
        List<Item> items = remoteClient.getAllItems();
        for (Item item : items) {
            String itemInfo = "Item Name: " + item.getName().toString()
                    + "\t\tCurrent Price: " + item.getBidding_price();
            model.addElement(itemInfo);
        }
        itemList.setModel(model);
    }
    private void showItemDetail(Item item) {
        DefaultListModel<String> detailModel = new DefaultListModel<>();
        String itemName = "Item Name: " + item.getName().toString();
        String itemBrand = "Brand: " + item.getBrand().toString();
        String itemStartPrice = "Start Price: " + Float.toString(item.getStarting_price());
        String itemCurrentPrice = "Current Price: " + Float.toString(item.getBidding_price());
        String itemBidHolder = "Bid Holder: " + item.getHighest_bidder();
        detailModel.addElement(itemName);
        detailModel.addElement(itemBrand);
        detailModel.addElement(itemStartPrice);
        detailModel.addElement(itemCurrentPrice);detailModel.addElement(itemBidHolder);
        itemDetail.setModel(detailModel);
    }
    private void clearItemDetail(){
        DefaultListModel<String> emptyModel = new DefaultListModel<>();
        itemDetail.setModel(emptyModel);
    }
    public void showServerMessage(String msg){
        temp.addElement(msg);
    }
    public void updateServerMessage(){
        serverMessageList.setModel(temp);
    }
    private void showMessage(String message) {
        Dialog dialog = new Dialog(this, "Message");
        dialog.setBounds(300, 200, 400, 100);
        Label label = new Label(message);
        label.setBounds(30, 30, 200, 30);
        dialog.add(label);
        dialog.setLayout(null);
        dialog.setVisible(true);
        Button closeButton = new Button("Close");
        closeButton.setBounds(125, 60, 100, 30);
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // close the dialog
                dialog.dispose();
            }
        });
        dialog.add(closeButton);
        dialog.setLayout(null);
        dialog.setVisible(true);
    }

        private void bidderLogOut(String contact_info) throws RemoteException{
            boolean isLoggedOut = remoteClient.logOut(contact_info);
            if(isLoggedOut){
                dispose();
                client.changeFrame(new Login(remoteClient, client));
            }
        }
    }